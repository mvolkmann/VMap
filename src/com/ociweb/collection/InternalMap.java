package com.ociweb.collection;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * This is used to store the entries for VHashMap objects.
 * @param <K> the key type
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class InternalMap<K, V> {

    //private static final float LOAD_FACTOR_LIMIT = 0.75f;
    static final int INITIAL_BUCKET_COUNT = 11;

    private VMapEntry<K, V>[] buckets;
    private int size;

    /**
     * Creates an InternalMap with the default initial capacity.
     */
    InternalMap() {
        this(INITIAL_BUCKET_COUNT);
    }

    /**
     * Creates an InternalMap with a specified initial capacity.
     * @param bucketCount the initial capacity
     */
    @SuppressWarnings("unchecked")
    InternalMap(int bucketCount) {
        // Can't create generic arrays in Java!
        //buckets = new VMapEntry<K, V>[bucketCount];
        buckets = (VMapEntry<K, V>[])
            Array.newInstance(VMapEntry.class, bucketCount);
    }

    /**
     * Determines whether this map contains a given key
     * that is present in a given version of this map.
     * @param version
     * @param key
     * @return true if present; false otherwise
     */
    final synchronized boolean contains(Version version, K key) {
        return get(version, key) != null;
    }

    /**
     * Deletes any number of entries from a given version of this map.
     * They are "deleted" by adding a null value for the keys.
     * This means there is no way to distinguish between
     * keys that are missing and keys that are present with a null value.
     * @param version the Version
     * @param keys the keys of the entries to be deleted
     * @return the number of entries that were deleted.
     */
    final synchronized int delete(Version version, K... keys) {
        int deletedCount = 0;

        for (K key : keys) {
            VMapEntry<K, V> entry = getEntry(key);
            if (entry != null) {
                entry.addValue(version, null);
                size--;
                deletedCount++;
            }
        }

        return deletedCount;
    }

    /**
     * Dumps the contents of this map to stdout
     * in a form that is useful for debugging.
     */
    final synchronized public void dump() {
        if (buckets == null) {
            System.out.println("  empty");
            return;
        }

        for (int i = 0; i < buckets.length; i++) {
            System.out.println("bucket " + i);
            VMapEntry<K, V> entry = buckets[i];
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
     * Gets the value for a given key in a given version of this map.
     * @param version the Version
     * @param key the key
     * @return the value
     */
    final synchronized V get(Version version, K key) {
        VMapEntry<K, V> entry = getEntry(key);
        return entry == null ? null : entry.getValue(version);
    }

    /**
     * Gets the bucket index of a given key.
     * @param key the key
     * @return the bucket index
     */
    private int getBucketIndex(K key) {
        return getBucketIndex(key.hashCode());
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
     * Gets the entry object for a given key.
     * @param value the value
     * @return the VMapEntry
     */
    private VMapEntry<K, V> getEntry(K key) {
        int bucketIndex = getBucketIndex(key);
        return getEntry(bucketIndex, key);
    }

    /**
     * Gets the entry object for a given bucket index and value.
     * @param bucketIndex the bucket index
     * @param value the value
     * @return the VMapEntry
     */
    private VMapEntry<K, V> getEntry(
        int bucketIndex, K key) {

        VMapEntry<K, V> entry = buckets[bucketIndex];
        while (entry != null) {
            // TODO: Is it faster to only compare keys?
            //if (entry.hashCode == hashCode && entry.key.equals(key)) {
            if (entry.key.equals(key)) return entry;
            entry = entry.next;
        }

        //System.out.println("InternalMap.getEntry: not found");
        return null;
    }

    /**
     * Gets the first entry object in this map.
     * @return the first VMapEntry
     */
    private VMapEntry<K, V> getFirstEntry() {
        return getNextEntry(0);
    }

    final synchronized VMapEntry<K, V> getNextEntry(
        VMapEntry<K, V> prev) {

        VMapEntry<K, V> next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return getNextEntry(bucketIndex + 1);
    }

    private VMapEntry<K, V> getNextEntry(int bucketIndex) {
        while (bucketIndex < buckets.length) {
            VMapEntry<K, V> next = buckets[bucketIndex];
            if (next != null) return next;
            bucketIndex++;
        }

        return null;
    }

    /**
     * Iterates through the VMapEntry objects in this InternalMap.
     */
    final Iterator<VMapEntry> iterator(Version version) {
        return new MyIterator<K, V>(version, this);
    }

    /**
     * @return true if a new entry was added;
     *         false if the value of an existing entry was changed
     */
    final synchronized boolean put(Version version, K key, V value) {
        boolean added = true; // assume

        int bucketIndex = getBucketIndex(key);

        VMapEntry<K, V> entry = getEntry(bucketIndex, key);
        if (entry == null) {
            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VMapEntry<K, V>(key, entry);

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

        entry.addValue(version, value);

        return added;
    }

    /**
     * @return the number of entries that were added
     */
    final synchronized int put(Version version, Pair<K, V>... pairs) {
        int addedCount = 0;

        for (Pair<K, V> pair : pairs) {
            if (put(version, pair.key, pair.value)) addedCount++;
        }

        return addedCount;
    }

    final synchronized void rehash() {
        int newBucketCount = (buckets.length * 2) + 1;

        @SuppressWarnings("unchecked")
        VMapEntry<K, V>[] newBuckets = (VMapEntry<K, V>[])
            Array.newInstance(VMapEntry.class, newBucketCount);

        byte[] newBucketLengths = new byte[newBucketCount];

        for (int bucketIndex = 0; bucketIndex < buckets.length; bucketIndex++) {
            VMapEntry<K, V> entry = buckets[bucketIndex];
            while (entry != null) {
                VMapEntry<K, V> nextEntry = entry.next;

                int newBucketIndex =
                    (int) (Math.abs(entry.hashCode) % newBucketCount);

                // Get current first entry in bucket.
                VMapEntry<K, V> newEntry = newBuckets[newBucketIndex];

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
        return "InternalMap with " + size + " entries";
    }

    static class MyIterator<K, V> implements Iterator<VMapEntry> {

        InternalMap<K, V> map;
        Version version;
        VMapEntry<K, V> next;

        MyIterator(Version version, InternalMap<K, V> map) {
            this.version = version;
            this.map = map;
            setNext();
        }

        @Override
        public boolean hasNext() { return next != null; }

        @Override
        public VMapEntry<K, V> next() {
            VMapEntry<K, V> result = next;
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