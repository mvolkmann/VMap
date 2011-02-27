package com.ociweb.collection;

import com.ociweb.lang.MutableInteger;
import java.util.Iterator;

/**
 * A versioned, immutable hash map.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <K> the key type
 * @param <V> the value type
 */
public class VHashMap<K, V> implements VMap<K, V> {

    private MutableInteger highestVersion = new MutableInteger();
    private InternalMap<K, V> map;
    private Version unusedVersion;
    private Version version;
    //private VHashMap<K, V> parent; // TODO: Want this?
    private int size;

    /**
     * Creates an empty VHashMap with the default initial capacity.
     */
    public VHashMap() {
        version = new Version();
        map = new InternalMap<K, V>(InternalMap.INITIAL_BUCKET_COUNT);
    }

    /**
     * Creates an empty VHashMap with a given initial capacity.
     * @param initialCapacity the initial capacity
     */
    public VHashMap(int initialCapacity) {
        version = new Version();
        map = new InternalMap<K, V>(initialCapacity);
    }

    /**
     * Creates a VHashMap with given key/value pairs
     * @param pairs any number of compatible Pair objects
     */
    public VHashMap(Pair<K, V>... pairs) {
        version = new Version();
        size = pairs.length;
        map = new InternalMap<K, V>(size);
        map.put(version, pairs);
    }

    /**
     * Creates the next version of a given VHashMap.
     * @param parent the parent VHashMap to the new one
     * @param version the version of the new one
     */
    private VHashMap(VHashMap<K, V> parent, Version version) {
        synchronized (this) {
            // Share internal map with parent version.
            map = parent.map;

            size = parent.size;
            highestVersion = parent.highestVersion;
            this.version = version;
            //this.parent = parent;
        }
    }

    @Override
    public final synchronized boolean containsKey(K key) {
        return map.contains(version, key);
    }

    @Override
    public final synchronized VMap<K, V> delete(K... keys) {
        Version nextVersion = getNextVersion();
        int deletedCount = map.delete(nextVersion, keys);
        if (deletedCount == 0) {
            unusedVersion = nextVersion;
            return this;
        }

        VHashMap<K, V> newMap = new VHashMap<K, V>(this, nextVersion);
        newMap.size -= deletedCount;
        unusedVersion = null;
        return newMap;
    }

    @Override
    public final synchronized void dump(String name) {
        System.out.println("<<< start of VHashMap dump of " + name);
        System.out.println(this);
        map.dump();
        System.out.println(">>> end of VHashMap dump of " + name + '\n');
    }

    /**
     * Indicates whether some object is "equal" to this one.
     * @param obj the object with which to compare
     * @return true if equal; false otherwise
     */
    @Override
    public final boolean equals(Object obj) {
        // Next line makes NetBeans happy.
        if (!(obj instanceof VHashMap)) return false;
        return obj == this;
    }

    @Override
    public final synchronized V get(K key) {
        return map.get(version, key);
    }

    private Version getNextVersion() {
        if (unusedVersion != null) return unusedVersion;
        if (version.number == Integer.MAX_VALUE) throw new VersionException();
        return new Version(highestVersion, version);
    }

    @Override
    public final int getVersionNumber() { return version.number; }

    /**
     * Throws UnsupportedOperationException because
     * VHashMap objects cannot be used as keys in hash tables.
     */
    @Override
    public final int hashCode() {
        throw new UnsupportedOperationException(
            "cannot use as a key in a map or set");
    }

    @Override
    public final Iterator<K> keyIterator() {
        return new KeyIterator<K>();
    }

    @Override
    public final synchronized VMap<K, V> put(K key, V value) {
        Version nextVersion = getNextVersion();
        InternalMap.PutAction putAction = map.put(nextVersion, key, value);
        if (putAction == InternalMap.PutAction.NONE) {
            unusedVersion = nextVersion;
            return this;
        }

        VHashMap<K, V> newMap = new VHashMap<K, V>(this, nextVersion);
        if (putAction == InternalMap.PutAction.ADDED_ENTRY) newMap.size++;
        unusedVersion = null;
        return newMap;
    }

    @Override
    public final synchronized VMap<K, V> put(Pair<K, V>... pairs) {
        Version nextVersion = getNextVersion();
        int addedCount = map.put(nextVersion, pairs);
        if (addedCount == 0) {
            unusedVersion = nextVersion;
            return this;
        }

        VHashMap<K, V> newMap = new VHashMap<K, V>(this, nextVersion);
        newMap.size += addedCount;
        unusedVersion = null;
        return newMap;
    }

    @Override
    public final int size() { return size; }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public final String toString() {
        return "VHashMap: " + version + ", size=" + size;
    }

    @Override
    public final Iterator<V> valueIterator() {
        return new ValueIterator<V>();
    }

    class KeyIterator<K> implements Iterator<K> {

        private Iterator<VMapEntry> iterator = map.iterator(version);

        @Override
        // TODO: Failing to take version into account!
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        // TODO: Failing to take version into account!
        public K next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.key;
        }

        @Override
        public void remove() { iterator.remove(); }
    }

    class ValueIterator<V> implements Iterator<V> {

        private Iterator<VMapEntry> iterator = map.iterator(version);

        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public V next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.getValue(version);
        }

        @Override
        public void remove() { iterator.remove(); }
    }
}