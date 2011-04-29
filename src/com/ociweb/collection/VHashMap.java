package com.ociweb.collection;

import com.ociweb.lang.MutableInteger;
import java.util.Iterator;

/**
 * A versioned, logically immutable hash map.
 * @param <K> the key type
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
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
        map = new InternalMap<K, V>(pairs.length);
        size = map.put(version, pairs);
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

    // Javadoc comes from the VMap interface.
    @Override
    public final synchronized boolean containsKey(K key) {
        return map.contains(version, key);
    }

    // Javadoc comes from the VMap interface.
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

    // Javadoc comes from the VMap interface.
    @Override
    public final synchronized void dump(String name, boolean includeContent) {
        System.out.println("\n<<< start of VHashMap dump of " + name);
        System.out.println("size = " + size());
        System.out.println(this);
        map.dump(includeContent);
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

    /**
     * Gets the next version of this set that can be created.
     * @return the Version
     */
    private Version getNextVersion() {
        if (unusedVersion != null) return unusedVersion;
        if (version.number == Integer.MAX_VALUE) throw new VersionException();
        return new Version(highestVersion, version);
    }

    // Javadoc comes from the VMap interface.
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

    // Javadoc comes from the VMap interface.
    @Override
    public final Iterator<K> keyIterator() {
        return new KeyIterator<K>();
    }

    // Javadoc comes from the VMap interface.
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

    // Javadoc comes from the VMap interface.
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

    // Javadoc comes from the VMap interface.
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

    /**
     * An Iterator for iterating through the keys of this map.
     * @param <K> the key type
     */
    class KeyIterator<K> implements Iterator<K> {

        private Iterator<VMapEntry> iterator = map.iterator(version);

        /**
         * Determines whether that is another key to visit.
         * @return true if so; false otherwise
         */
        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public K next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.key;
        }

        /**
         * Removing entries from this iterator is not supported.
         */
        @Override
        public void remove() { iterator.remove(); }
    }

    /**
     * An Iterator for iterating through the values of this map.
     * @param <V> the value type
     */
    class ValueIterator<V> implements Iterator<V> {

        private Iterator<VMapEntry> iterator = map.iterator(version);

        /**
         * Determines whether that is another value to visit.
         * @return true if so; false otherwise
         */
        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        /**
         * Gets the next value.
         * @return the next value
         */
        @Override
        public V next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.getValue(version);
        }

        /**
         * Removing entries from this iterator is not supported.
         */
        @Override
        public void remove() { iterator.remove(); }
    }
}