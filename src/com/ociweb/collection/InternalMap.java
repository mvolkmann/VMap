package com.ociweb.collection;

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Iterator;

class InternalMap<K, V> {

    //private static final double LOAD_FACTOR_LIMIT = 1.5; //0.75;
    private static final int INITIAL_BUCKET_COUNT = 11;

    private VMapEntry<K, V>[] buckets;
    private int size;

    InternalMap() {
        this(INITIAL_BUCKET_COUNT);
    }

    @SuppressWarnings("unchecked")
    InternalMap(int bucketCount) {
        //log("ctor", "bucketCount", bucketCount);
        //buckets = new VMapEntry<K, V>[bucketCount];
        buckets = (VMapEntry<K, V>[])
            Array.newInstance(VMapEntry.class, bucketCount);
    }

    boolean contains(BitSet versionSet, K key) {
        return get(versionSet, key) != null;
    }

    void delete(int version, K... keys) {
        for (K key : keys) {
            VMapEntry<K, V> entry = getEntry(key);
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
            VMapEntry<K, V> entry = buckets[i];
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

    V get(BitSet versionSet, K key) {
        VMapEntry<K, V> entry = getEntry(key);
        return entry == null ? null : entry.getValue(versionSet);
    }

    private int getBucketIndex(K key) {
        return getBucketIndex(key.hashCode());
    }

    private int getBucketIndex(long hashCode) {
        return (int) (Math.abs(hashCode) % buckets.length);
    }

    private VMapEntry<K, V> getEntry(K key) {
        int bucketIndex = getBucketIndex(key);
        return getEntry(bucketIndex, key);
    }

    private VMapEntry<K, V> getEntry(
        int bucketIndex, K key) {

        long hashCode = key.hashCode();
        /*
        System.out.println("InternalMap.getEntry: " +
            "bucketIndex=" + bucketIndex +
            ", key=" + key +
            ", hashCode=" + hashCode);
        */

        VMapEntry<K, V> entry = buckets[bucketIndex];
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

    VMapEntry<K, V> getFirstEntry() { return getNextEntry(0); }

    VMapEntry<K, V> getNextEntry(VMapEntry<K, V> prev) {
        VMapEntry<K, V> next = prev.next;
        if (next != null) return next;

        int bucketIndex = getBucketIndex(prev.hashCode);
        return getNextEntry(bucketIndex + 1);
    }

    VMapEntry<K, V> getNextEntry(int bucketIndex) {
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
    Iterator<VMapEntry> iterator() { return new MyIterator<K, V>(this); }

    private void log(String method, Object msg) {
        System.out.println("InternalMap." + method + ": " + msg);
    }

    private void log(String method, String name, Object value) {
        log(method, name + " = " + value);
    }

    void put(int version, K key, V value) {
        int bucketIndex = getBucketIndex(key);
        /*
        System.out.println("InternalMap.put: version=" + version +
            ", key=" + key +
            ", value=" + value +
            ", bucketIndex=" + bucketIndex);
        */

        VMapEntry<K, V> entry = getEntry(bucketIndex, key);
        if (entry == null) {
            // Get first entry in proper bucket.
            entry = buckets[bucketIndex];

            // Create new entry.
            entry = new VMapEntry<K, V>(key, entry);

            // Make it the first entry in the bucket.
            buckets[bucketIndex] = entry;

            size++;
            //double loadFactor = size / buckets.length;
            //if (loadFactor > LOAD_FACTOR_LIMIT) rehash();
            if (size > buckets.length) rehash();
        }

        entry.addValue(version, value);
    }

    void put(int version, Pair<K, V>... pairs) {
        for (Pair<K, V> pair : pairs) {
            put(version, pair.key, pair.value);
        }
    }

    void rehash() {
        int newBucketCount = (buckets.length * 2) + 1;
        //log("rehash", "newBucketCount", newBucketCount);

        //VMapEntry<K, V>[] newBuckets = new VMapEntry<K, V>[newBucketCount];
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

    int size() { return size; }

    @Override
    public String toString() {
        return "InternalMap with " + size + " entries";
    }

    class MyIterator<K, V> implements Iterator<VMapEntry> {

        InternalMap<K,V> map;
        VMapEntry<K, V> nextEntry;
        int seenCount;

        MyIterator(InternalMap<K,V> map) { this.map = map; }

        @Override
        public boolean hasNext() { return seenCount < size; }

        @Override
        public VMapEntry<K, V> next() {
            nextEntry = nextEntry == null ?
                map.getFirstEntry() : map.getNextEntry(nextEntry);
            if (nextEntry != null) seenCount++;
            return nextEntry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove elements");
        }
    }
}