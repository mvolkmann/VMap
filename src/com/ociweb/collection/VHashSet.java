package com.ociweb.collection;

import com.ociweb.lang.MutableInteger;
import java.util.Iterator;

/**
 * A versioned, immutable hash set.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <V> the value type
 */
public class VHashSet<V> implements VSet<V> {

    private MutableInteger highestVersion = new MutableInteger();
    private InternalSet<V> set;
    private Version version;
    //private VHashSet<V> parent; // TODO: Want this?
    private int size;

    /**
     * Creates an empty VHashSet with the default initial capacity.
     */
    public VHashSet() {
        version = new Version();
        set = new InternalSet<V>(InternalSet.INITIAL_BUCKET_COUNT);
    }

    /**
     * Creates an empty VHashSet with a given initial capacity.
     * @param initialCapacity the initial capacity
     */
    public VHashSet(int initialCapacity) {
        version = new Version();
        set = new InternalSet<V>(initialCapacity);
    }

    /**
     * Creates a VHashSet with given compatible values.
     * @param values any number of compatible values
     */
    public VHashSet(V... values) {
        version = new Version();
        size = values.length;
        set = new InternalSet<V>(values.length);
        set.add(version, values);
    }

    /**
     * Creates the next version of a given VHashSet.
     * @param parent the parent VHashSet to the new one
     */
    private VHashSet(VHashSet<V> parent) {
        if (parent.version.number == Integer.MAX_VALUE) {
            throw new VersionException();
        }

        synchronized (this) {
            // Share internal set with parent version.
            set = parent.set;

            size = parent.size;
            highestVersion = parent.highestVersion;
            version = new Version(highestVersion, parent.version);
            //this.parent = parent;
        }
    }

    @Override
    public final synchronized VSet<V> add(V... values) {
        VHashSet<V> newSet = new VHashSet<V>(this);
        newSet.size += newSet.set.add(newSet.version, values);
        return newSet;
    }

    @Override
    public final synchronized boolean contains(V value) {
        return set.contains(version, value);
    }

    @Override
    public final synchronized VSet<V> delete(V... values) {
        VHashSet<V> newSet = new VHashSet<V>(this);
        int deleteCount = newSet.set.delete(newSet.version, values);
        newSet.size -= deleteCount;
        return newSet;
    }

    @Override
    public final synchronized void dump() {
        System.out.println("<<< start of VHashSetDump");
        System.out.println(this);
        set.dump();
        System.out.println(">>> end of VHashSet dump");
    }

    /**
     * Indicates whether some object is "equal" to this one.
     * @param obj the object with which to compare
     * @return true if equal; false otherwise
     */
    @Override
    public final boolean equals(Object obj) {
        // Next line makes NetBeans happy.
        if (!(obj instanceof VHashSet)) return false;
        return obj == this;
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
    public final Iterator<V> iterator() {
        return new VHashSetIterator<V>();
    }

    @Override
    public final int size() { return size; }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public final String toString() {
        return "VHashSet: " + version;
    }

    class VHashSetIterator<V> implements Iterator<V> {

        private Iterator<VSetEntry> iterator = set.iterator(version);

        @Override
        // TODO: Failing to take version into account!
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        // TODO: Failing to take version into account!
        public V next() {
            @SuppressWarnings("unchecked")
            VSetEntry<V> entry = iterator.next();
            return entry == null ? null : entry.value;
        }

        @Override
        public void remove() { iterator.remove(); }
    }
}