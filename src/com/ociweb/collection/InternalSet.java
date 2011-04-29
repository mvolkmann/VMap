package com.ociweb.collection;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * This is used to store the entries for VHashSet objects.
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class InternalSet<V> {

    private static final float LOAD_FACTOR_LIMIT = 0.75f;
    static final int INITIAL_BUCKET_COUNT = 11;

    private VSetEntry<V>[] buckets;
    private int entryCount;
    private int rehashCount;

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
            // No entry was found for the value, so add one.

            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VSetEntry<V>(value, entry);
            entry.add(version.number, true);

            // Make it the first entry in the bucket.
            buckets[bucketIndex] = entry;

            entryCount++;

            // TODO: Is performance better when using load factor?
            float loadFactor = ((float) entryCount) / buckets.length;
            if (loadFactor > LOAD_FACTOR_LIMIT) rehash();
            //if (entryCount > buckets.length) rehash();
        } else {
            // An entry was found for the value.

            VersionMember vm = entry.getVersionMember(version);
            // If it is marked as present in this version ...
            if (vm.member) {
                // There is no need to add it.
                added = false;
            } else {
                // Make it present in this version.
                entry.add(version.number, true);
            }
        }

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
        return entry == null ? false : entry.contains(version, true);
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
            if (entry != null && entry.contains(version, true)) {
                entry.add(version.number, false);
                deletedCount++;
            }
        }

        return deletedCount;
    }

    /**
     * Dumps the contents of this set to stdout
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
        VSetEntry<V> entry = buckets[bucketIndex];
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
     * Dumps statistics about this set to stdout for debugging.
     */
    final synchronized void dumpStats() {
        int emptyBucketCount = 0;
        int maxEntryListLength = 0;
        int maxValueListLength = 0;

        for (int i = 0; i < buckets.length; i++) {
            VSetEntry entry = buckets[i];

            if (entry == null) {
                emptyBucketCount++;
            } else {
                int entryListLength = 0;

                do {
                    entryListLength++;

                    int valueListLength = entry.getValueListLength();
                    if (valueListLength > maxValueListLength) {
                        maxValueListLength = valueListLength;
                    }

                    entry = entry.next;
                } while (entry != null);

                if (entryListLength > maxEntryListLength) {
                    maxEntryListLength = entryListLength;
                    //dumpBucket(i);
                }
            }
        }

        System.out.println("bucket count: " + buckets.length);
        System.out.println("empty bucket count: " + emptyBucketCount);
        System.out.println("entry count:" + entryCount);
        System.out.println("max entry list length: " + maxEntryListLength);
        System.out.println("max value list length: " + maxValueListLength);
        System.out.println("rehash count: " + rehashCount);
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
            if (entry.value.equals(value)) return entry;
            entry = entry.next;
        }
        return null;
    }

    /**
     * Gets the first entry object in this set.
     * @return the first VSetEntry
     */
    private VSetEntry<V> getFirstEntry() { return getNextEntry(0); }

    /**
     * Gets the first entry object in this set after a given one.
     * @param prev the previous entry
     * @return the next VSetEntry
     */
    private VSetEntry<V> getNextEntry(VSetEntry<V> prev) {
        VSetEntry<V> next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return getNextEntry(bucketIndex + 1);
    }

    /**
     * Gets the next entry object in this set starting at a given bucket.
     * @param bucketIndex the bucket index
     * @return the next VSetEntry
     */
    private VSetEntry<V> getNextEntry(int bucketIndex) {
        while (bucketIndex < buckets.length) {
            VSetEntry<V> next = buckets[bucketIndex];
            if (next != null) return next;
            bucketIndex++;
        }

        return null;
    }

    /**
     * Gets an Iterator for iterating through the VSetEntry objects
     * in a given version of this set.
     * @param version the Version
     * @return the Iterator
     */
    final Iterator<VSetEntry> iterator(Version version) {
        return new MyIterator<V>(version, this);
    }

    /**
     * Rehashes this set by increasing the number of buckets and
     * redistributing the set entries.
     */
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

        rehashCount++;
    }

    /**
     * Gets the string representation of this object
     * @return the string representation
     */
    @Override
    public final String toString() {
        return "InternalSet with " + entryCount + " entries";
    }

    /**
     * An Iterator for iterating through the entries in a given set.
     * @param <V> the value type
     */
    static class MyIterator<V> implements Iterator<VSetEntry> {

        InternalSet<V> set;
        Version version;
        VSetEntry<V> next;

        /**
         * Creates an iterator for a given version of a given set.
         * @param version the Version
         * @param set the InternalSet
         */
        MyIterator(Version version, InternalSet<V> set) {
            this.version = version;
            this.set = set;
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
        public VSetEntry<V> next() {
            VSetEntry<V> result = next;
            setNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove elements");
        }

        /**
         * Sets the next entry that will be returned by the next method.
         */
        private void setNext() {
            next = next == null ?  set.getFirstEntry() : set.getNextEntry(next);
            while (next != null && !next.contains(version, true)) {
                next = set.getNextEntry(next);
            }
        }
    }
}