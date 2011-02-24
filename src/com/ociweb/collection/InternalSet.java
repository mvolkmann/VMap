package com.ociweb.collection;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * This is used to store the entries for VHashSet objects.
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class InternalSet<V> {

    //private static final float LOAD_FACTOR_LIMIT = 0.75f;
    static final int INITIAL_BUCKET_COUNT = 11;

    private VSetEntry<V>[] buckets;
    private int size;

    /**
     * Creates an InternalSet with the default initial capacity.
     */
    InternalSet() {
        this(INITIAL_BUCKET_COUNT);
    }

    /**
     * Creates an InternalSet with a specified initial capacity.
     * @param bucketCount the initial capacity
     */
    @SuppressWarnings("unchecked")
    InternalSet(int bucketCount) {
        // Can't create generic arrays in Java!
        buckets = (VSetEntry<V>[])
            Array.newInstance(VSetEntry.class, bucketCount);
    }

    /**
     * Adds a value to a given version of this set.
     * @param version the Version
     * @param value the value
     * @return true if a new entry was added;
     *         false if the value of an existing entry was changed
     */
    final synchronized boolean add(Version version, V value) {
        boolean added = true; // assume

        int bucketIndex = getBucketIndex(value);

        VSetEntry<V> entry = getEntry(bucketIndex, value);
        if (entry == null) {
            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VSetEntry<V>(value, entry);

            // Make it the first entry in the bucket.
            buckets[bucketIndex] = entry;

            size++;

            // The performance test for VHashSet does worse using load factor.
            //float loadFactor = ((float) size) / buckets.length;
            //if (loadFactor > LOAD_FACTOR_LIMIT) rehash();
            if (size > buckets.length) rehash();
        } else {
            added = false;
        }

        entry.add(version, true);

        return added;
    }

    /**
     * Adds any number of values to a given version of this set.
     * @param version the Version
     * @param values the values
     * @return the number of entries that were added
     */
    final synchronized int add(Version version, V... values) {
        int addedCount = 0;

        for (V value : values) {
            if (add(version, value)) addedCount++;
        }

        return addedCount;
    }

    /**
     * Determines whether this set contains a given value
     * in a given version of this set.
     * @param version
     * @param value
     * @return true if present; false otherwise
     */
    final synchronized boolean contains(Version version, V value) {
        VSetEntry<V> entry = getEntry(value);
        return entry == null ? false : entry.contains(version);
    }

    /**
     * Deletes any number of entries from a given version of this set.
     * They are "deleted" by adding a false member for the values.
     * @param version the Version
     * @param values the values to be deleted
     * @return the number of entries that were deleted.
     */
    final synchronized int delete(Version version, V... values) {
        int deletedCount = 0;

        for (V value : values) {
            VSetEntry<V> entry = getEntry(value);
            if (entry != null) {
                entry.add(version, false);
                size--;
                deletedCount++;
            }
        }

        return deletedCount;
    }


    /**
     * Dumps the contents of this set to stdout
     * in a form that is useful for debugging.
     */
    final synchronized void dump() {
        if (buckets == null) {
            System.out.println("  empty");
            return;
        }

        for (int i = 0; i < buckets.length; i++) {
            System.out.println("bucket " + i);
            VSetEntry<V> entry = buckets[i];
            if (entry == null) {
                System.out.println("  empty");
            } else {
                while (entry != null) {
                    System.out.println(entry);
                    entry = entry.next;
                }
            }
        }
    }

    /**
     * Gets the bucket index of a given value.
     * @param value the value
     * @return the bucket index
     */
    private int getBucketIndex(V value) {
        return getBucketIndex(value.hashCode());
    }

    /**
     * Gets the bucket index of a given hash code.
     * @param hashCode the hash code
     * @return the bucket index
     */
    private int getBucketIndex(long hashCode) {
        return (int) (Math.abs(hashCode) % buckets.length);
    }

    /**
     * Gets the entry object for a given value.
     * @param value the value
     * @return the VSetEntry
     */
    private VSetEntry<V> getEntry(V value) {
        int bucketIndex = getBucketIndex(value);
        return getEntry(bucketIndex, value);
    }

    /**
     * Gets the entry object for a given bucket index and value.
     * @param bucketIndex the bucket index
     * @param value the value
     * @return the VSetEntry
     */
    private VSetEntry<V> getEntry(int bucketIndex, V value) {
        VSetEntry<V> entry = buckets[bucketIndex];
        while (entry != null) {
            // TODO: Is it faster to only compare keys?
            //if (entry.hashCode == hashCode && entry.key.equals(value)) {
            if (entry.value.equals(value)) return entry;
            entry = entry.next;
        }

        //System.out.println("InternalSet.getEntry: not found");
        return null;
    }

    /**
     * Gets the first entry object in this set.
     * @return the first VSetEntry
     */
    private VSetEntry<V> getFirstEntry() { return getNextEntry(0); }

    private VSetEntry<V> getNextEntry(VSetEntry<V> prev) {
        VSetEntry<V> next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return getNextEntry(bucketIndex + 1);
    }

    private VSetEntry<V> getNextEntry(int bucketIndex) {
        while (bucketIndex < buckets.length) {
            VSetEntry<V> next = buckets[bucketIndex];
            if (next != null) return next;
            bucketIndex++;
        }

        return null;
    }

    /**
     * Iterates through the VSetEntry objects in this InternalSet.
     */
    final Iterator<VSetEntry> iterator(Version version) {
        return new MyIterator<V>(version, this);
    }

    final synchronized void rehash() {
        int newBucketCount = (buckets.length * 2) + 1;

        @SuppressWarnings("unchecked")
        VSetEntry<V>[] newBuckets = (VSetEntry<V>[])
            Array.newInstance(VSetEntry.class, newBucketCount);

        byte[] newBucketLengths = new byte[newBucketCount];

        for (int bucketIndex = 0; bucketIndex < buckets.length; bucketIndex++) {
            VSetEntry<V> entry = buckets[bucketIndex];
            while (entry != null) {
                VSetEntry<V> nextEntry = entry.next;

                int newBucketIndex =
                    (int) (Math.abs(entry.hashCode) % newBucketCount);

                // Get current first entry in bucket.
                VSetEntry<V> newEntry = newBuckets[newBucketIndex];

                // Change existing entry to point to current first entry.
                entry.next = newEntry;

                // Change existing entry to be
                // the new first entry in the bucket.
                newBuckets[newBucketIndex] = entry;

                newBucketLengths[newBucketIndex]++;

                entry = nextEntry;
            }
        }

        buckets = newBuckets; // Previous buckets will be GCed.
    }

    @Override
    public final String toString() {
        return "InternalSet with " + size + " entries";
    }

    static class MyIterator<V> implements Iterator<VSetEntry> {

        InternalSet<V> map;
        Version version;
        VSetEntry<V> next;

        MyIterator(Version version, InternalSet<V> map) {
            this.version = version;
            this.map = map;
            setNext();
        }

        @Override
        public boolean hasNext() { return next != null; }

        @Override
        public VSetEntry<V> next() {
            VSetEntry<V> result = next;
            setNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove elements");
        }

        private void setNext() {
            next = next == null ?  map.getFirstEntry() : map.getNextEntry(next);
            while (next != null && !next.contains(version)) {
                next = map.getNextEntry(next);
            }
        }
    }
}