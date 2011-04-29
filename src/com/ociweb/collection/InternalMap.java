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

    /**
     * When adding a value key/value pair there are three possible outcomes.
     * <ol>
     *   <li>NONE means the key was already present
     *    with the given value for the given version, so nothing was added.</li>
     *   <li>ADDED_ENTRY means the key was not present
     *     and a new entry was added.</li>
     *   <li>ADDED_VALUE means the key was present, but had a
     *    different value for the given version, so a new value was added.</li>
     * </ol>
     */
    enum PutAction { NONE, ADDED_ENTRY, ADDED_VALUE };

    private static final float LOAD_FACTOR_LIMIT = 0.75f;
    static final int INITIAL_BUCKET_COUNT = 11;

    private VMapEntry<K, V>[] buckets;
    private int entryCount;
    private int rehashCount;

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
                entry.addValue(version.number, null);
                deletedCount++;
            }
        }

        return deletedCount;
    }

    /**
     * Dumps the contents of this map to stdout
     * in a form that is useful for debugging.
     * @param detail true to include content; false for only high-level
     */
    final synchronized void dump(boolean includeContent) {
        dumpStats();
        if (!includeContent) return;

        if (buckets == null) {
            System.out.println("  empty");
        } else {
            for (int i = 0; i < buckets.length; i++) {
                if (buckets[i] != null) dumpBucket(i);
            }
        }
    }

    /**
     * Dumps statistics about a given bucket to stdout for debugging.
     */
    final synchronized void dumpBucket(int bucketIndex) {
        VMapEntry<K, V> entry = buckets[bucketIndex];
        System.out.println("bucket " + bucketIndex);
        if (entry == null) {
            System.out.println("  empty");
        } else {
            while (entry != null) {
                System.out.println(entry);
                entry = entry.next;
            }
        }
    }

    /**
     * Dumps statistics about this map to stdout for debugging.
     */
    final synchronized void dumpStats() {
        int emptyBucketCount = 0;
        int maxEntryListLength = 0;
        int maxValueListLength = 0;
        K maxValueListKey = null;

        for (int i = 0; i < buckets.length; i++) {
            VMapEntry<K, V> entry = buckets[i];

            if (entry == null) {
                emptyBucketCount++;
            } else {
                int entryListLength = 0;

                do {
                    entryListLength++;

                    int valueListLength = entry.getValueListLength();
                    if (valueListLength > maxValueListLength) {
                        maxValueListLength = valueListLength;
                        maxValueListKey = entry.key;
                    }

                    entry = entry.next;
                } while (entry != null);

                if (entryListLength > maxEntryListLength) {
                    maxEntryListLength = entryListLength;
                }
            }
        }

        System.out.println("bucket count: " + buckets.length);
        System.out.println("empty bucket count: " + emptyBucketCount);
        System.out.println("entry count:" + entryCount);
        System.out.println("max entry list length: " + maxEntryListLength);
        System.out.println("max value list length: " + maxValueListLength);
        System.out.println("max value list key: " + maxValueListKey);
        System.out.println("rehash count: " + rehashCount);
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
     * @param key the key
     * @return the VMapEntry
     */
    private VMapEntry<K, V> getEntry(K key) {
        int bucketIndex = getBucketIndex(key);
        return getEntry(bucketIndex, key);
    }

    /**
     * Gets the entry object for a given bucket index and value.
     * @param bucketIndex the bucket index
     * @param key the key
     * @return the VMapEntry
     */
    private VMapEntry<K, V> getEntry(int bucketIndex, K key) {
        VMapEntry<K, V> entry = buckets[bucketIndex];
        while (entry != null) {
            if (entry.key.equals(key)) return entry;
            entry = entry.next;
        }
        return null;
    }

    /**
     * Gets the first entry object in this map.
     * @return the first VMapEntry
     */
    private VMapEntry<K, V> getFirstEntry() { return getNextEntry(0); }

    /**
     * Gets the next entry object in this map after a given one.
     * @param prev the previous entry
     * @return the next VMapEntry
     */
    final synchronized VMapEntry<K, V> getNextEntry(VMapEntry<K, V> prev) {
        VMapEntry<K, V> next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return getNextEntry(bucketIndex + 1);
    }

    /**
     * Gets the next entry object in this map starting at a given bucket.
     * @param bucketIndex the bucket index
     * @return the next VMapEntry
     */
    private VMapEntry<K, V> getNextEntry(int bucketIndex) {
        while (bucketIndex < buckets.length) {
            VMapEntry<K, V> next = buckets[bucketIndex];
            if (next != null) return next;
            bucketIndex++;
        }

        return null;
    }

    /**
     * Gets an Iterator for iterating through the VMapEntry objects
     * in a given version of this set.
     * @param version the Version
     * @return the Iterator
     */
    final Iterator<VMapEntry> iterator(Version version) {
        return new MyIterator<K, V>(version, this);
    }

    /**
     * Adds a key/value pair to a given version of this map.
     * @param version the Version
     * @param key the key
     * @param value the value
     * @return the PutAction that was taken
     */
    final synchronized PutAction put(Version version, K key, V value) {
        PutAction putAction;

        int bucketIndex = getBucketIndex(key);
        VMapEntry<K, V> entry = getEntry(bucketIndex, key);
        if (entry == null) {
            // No entry was found for the key, so add one.

            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VMapEntry<K, V>(key, entry);
            entry.addValue(version.number, value);

            // Make it the first entry in the bucket.
            buckets[bucketIndex] = entry;

            entryCount++;

            // TODO: Is performance better when using load factor?
            float loadFactor = ((float) entryCount) / buckets.length;
            if (loadFactor > LOAD_FACTOR_LIMIT) rehash();
            //if (entryCount > buckets.length) rehash();

            putAction = PutAction.ADDED_ENTRY;
        } else {
            // An entry was found for the value.

            VersionValue vv = entry.getVersionValue(version);
            // If the value being added is already the value in this version ...
            if (value == vv.value || value.equals(vv.value)) {
                // There is no need to add it.
                putAction = PutAction.NONE;
            } else {
                // Make it present in this version.
                entry.addValue(version.number, value);
                putAction = PutAction.ADDED_VALUE;
            }
        }

        return putAction;
    }

    /**
     * Adds any number of key/value pairs to a given version of this map.
     * @param version the Version
     * @param pairs the key/value pairs
     * @return the number of entries that were added
     */
    final synchronized int put(Version version, Pair<K, V>... pairs) {
        int addedCount = 0;

        for (Pair<K, V> pair : pairs) {
            PutAction putAction = put(version, pair.key, pair.value);
            if (putAction == PutAction.ADDED_ENTRY) addedCount++;
        }

        return addedCount;
    }

    /**
     * Rehashes this map by increasing the number of buckets and
     * redistributing the map entries.
     */
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

        rehashCount++;
    }

    /**
     * Gets the string representation of this object
     * @return the string representation
     */
    @Override
    public final String toString() {
        return "InternalMap with " + entryCount + " entries";
    }

    /**
     * An Iterator for iterating through the entries in a given map.
     * @param <K> the key type
     * @param <V> the value type
     */
    static class MyIterator<K, V> implements Iterator<VMapEntry> {

        InternalMap<K, V> map;
        Version version;
        VMapEntry<K, V> next;

        /**
         * Creates an iterator for a given version of a given map.
         * @param version the Version
         * @param map the InternalMap
         */
        MyIterator(Version version, InternalMap<K, V> map) {
            this.version = version;
            this.map = map;
            setNext();
        }

        /**
         * Determines whether that is another entry to visit.
         * @return true if so; false otherwise
         */
        @Override
        public boolean hasNext() { return next != null; }

        /**
         * Gets the next entry.
         * @return the next entry
         */
        @Override
        public VMapEntry<K, V> next() {
            VMapEntry<K, V> result = next;
            setNext();
            return result;
        }

        /**
         * Removing entries from this iterator is not supported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove elements");
        }

        /**
         * Sets the next entry that will be returned by the next method.
         */
        private void setNext() {
            next = next == null ?  map.getFirstEntry() : map.getNextEntry(next);
            while (next != null && !next.contains(version)) {
                next = map.getNextEntry(next);
            }
        }
    }
}