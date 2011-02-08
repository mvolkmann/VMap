package com.ociweb.collection;

import java.util.BitSet;
import java.util.Iterator;

class InternalMap {

    private static final double LOAD_FACTOR_LIMIT = 1.5; //0.75;
    private static final int INITIAL_BUCKET_COUNT = 11;

    private VMapEntry[] buckets;
    private int size;

    InternalMap() {
        this(INITIAL_BUCKET_COUNT);
    }

    InternalMap(int bucketCount) {
        //log("ctor", "bucketCount", bucketCount);
        buckets = new VMapEntry[bucketCount];
    }

    boolean contains(BitSet versionSet, Object key) {
        return get(versionSet, key) != null;
    }

    void delete(int version, Object... keys) {
        for (Object key : keys) {
            VMapEntry entry = getEntry(key);
            if (entry != null) {
                entry.addValue(version, null);
                size--;
            }
        }
    }

    public void dump() {
        if (buckets == null) {
            System.out.println("  empty");
            return;
        }

        for (int i = 0; i < buckets.length; i++) {
            System.out.println("bucket " + i);
            VMapEntry entry = buckets[i];
            System.out.print("  ");
            if (entry == null) {
                System.out.println("empty");
            } else {
                while (entry != null) {
                    System.out.print(entry + " ");
                    entry = entry.next;
                }
                System.out.println();
            }
        }
    }

    VMapEntry firstEntry() { return nextEntry(0); }

    Object get(BitSet versionSet, Object key) {
        VMapEntry entry = getEntry(key);
        return entry == null ? null : entry.getValue(versionSet);
    }

    private int getBucketIndex(Object key) {
        return getBucketIndex(key.hashCode());
    }

    private int getBucketIndex(long hashCode) {
        return (int) (Math.abs(hashCode) % buckets.length);
    }

    private VMapEntry getEntry(Object key) {
        int bucketIndex = getBucketIndex(key);
        return getEntry(bucketIndex, key);
    }

    private VMapEntry getEntry(
        int bucketIndex, Object key) {

        long hashCode = key.hashCode();
        /*
        System.out.println("InternalMap.getEntry: " +
            "bucketIndex=" + bucketIndex +
            ", key=" + key +
            ", hashCode=" + hashCode);
        */

        VMapEntry entry = buckets[bucketIndex];
        while (entry != null) {
            // TODO: It may be faster to only compare keys.
            //if (entry.hashCode == hashCode && entry.key.equals(key)) {
            if (entry.key.equals(key)) {
                //System.out.println("InternalMap.getEntry: found");
                return entry;
            }
            entry = entry.next;
        }

        //System.out.println("InternalMap.getEntry: not found");
        return null;
    }

    Iterator iterator() { return new MyIterator(); }

    private void log(String method, Object msg) {
        System.out.println("InternalMap." + method + ": " + msg);
    }

    private void log(String method, String name, Object value) {
        log(method, name + " = " + value);
    }

    VMapEntry nextEntry(VMapEntry prev) {
        VMapEntry next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return nextEntry(bucketIndex + 1);
    }

    VMapEntry nextEntry(int bucketIndex) {
        while (bucketIndex < buckets.length) {
            VMapEntry next = buckets[bucketIndex];
            if (next != null) return next;
            bucketIndex++;
        }

        return null;
    }

    void put(int version, Object key, Object value) {
        int bucketIndex = getBucketIndex(key);
        /*
        System.out.println("InternalMap.put: version=" + version +
            ", key=" + key +
            ", value=" + value +
            ", bucketIndex=" + bucketIndex);
        */

        VMapEntry entry = getEntry(bucketIndex, key);
        if (entry == null) {
            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VMapEntry(key, entry);

            // Make it the first entry in the bucket.
            buckets[bucketIndex] = entry;

            size++;
            //double loadFactor = size / buckets.length;
            //if (loadFactor > LOAD_FACTOR_LIMIT) rehash();
            if (size > buckets.length) rehash();
        }

        entry.addValue(version, value);
    }

    void put(int version, Tuple... tuples) {
        for (Tuple tuple : tuples) {
            put(version, tuple.first, tuple.second);
        }
    }

    void rehash() {
        int newBucketCount = (buckets.length * 2) + 1;
        //log("rehash", "newBucketCount", newBucketCount);
        VMapEntry[] newBuckets = new VMapEntry[newBucketCount];
        byte[] newBucketLengths = new byte[newBucketCount];

        for (int bucketIndex = 0; bucketIndex < buckets.length; bucketIndex++) {
            VMapEntry entry = buckets[bucketIndex];
            while (entry != null) {
                VMapEntry nextEntry = entry.next;

                int newBucketIndex =
                    (int) (Math.abs(entry.hashCode) % newBucketCount);

                // Get current first entry in bucket.
                VMapEntry newEntry = newBuckets[newBucketIndex];

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

    int size() { return size; }

    @Override
    public String toString() {
        return "InternalMap with " + size + " entries";
    }

    class MyIterator implements Iterator {

        VMapEntry nextEntry;
        int seenCount;

        @Override
        public boolean hasNext() { return seenCount < size; }

        @Override
        public Object next() {
            nextEntry = nextEntry == null ?
                firstEntry() : nextEntry(nextEntry);
            if (nextEntry != null) seenCount++;
            return nextEntry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove elements");
        }
    }
}