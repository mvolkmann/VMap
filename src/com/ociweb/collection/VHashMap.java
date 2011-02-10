package com.ociweb.collection;

import java.util.BitSet;
import java.util.Iterator;

public class VHashMap<K, V> implements VMap<K, V> {

    private BitSet versionSet;
    private InternalMap<K, V> map;
    private int version;

    public VHashMap() {}

    public VHashMap(Pair<K, V>... pairs) {
        map = new InternalMap<K, V>(pairs.length);
        map.put(version, pairs);
        versionSet = new BitSet(1);
        versionSet.set(version); // zero
    }

    @Override
    public VMap<K, V> clear() {
        return new VHashMap<K, V>();
    }

    @Override
    public boolean containsKey(K key) {
        return map == null ? false : map.contains(versionSet, key);
    }

    private synchronized VHashMap<K, V> copy() {
        VHashMap<K, V> sim = new VHashMap<K, V>();
        sim.map = map == null ? new InternalMap<K, V>() : map;
        sim.version = version + 1;
        sim.versionSet = new BitSet(sim.version);
        if (versionSet != null) sim.versionSet.or(versionSet);
        sim.versionSet.set(sim.version);
        return sim;
    }

    @Override
    public VMap<K, V> delete(K... keys) {
        VHashMap<K, V> sim = copy();
        sim.map.delete(sim.version, keys);
        return sim;
    }

    @Override
    public void dump() {
        System.out.println("Map Dump:");
        if (map == null) {
            System.out.println("  empty");
        } else {
            map.dump();
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Next line makes NetBeans happy.
        if (!(obj instanceof VHashMap)) return false;
        return obj == this;
    }

    @Override
    public V get(K key) {
        return map == null ? null : map.get(versionSet, key);
    }

    @Override
    public long getVersion() { return version; }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
            "can't use as a key in a map or set");
    }

    @Override
    public Iterator<K> keyIterator() {
        return new KeyIterator<K>();
    }

    @Override
    public VMap<K, V> put(K key, V value) {
        VHashMap<K, V> sim = copy();
        sim.map.put(sim.version, key, value);
        return sim;
    }

    @Override
    public VMap<K, V> put(Pair<K, V>... pairs) {
        VHashMap<K, V> sim = copy();
        sim.map.put(sim.version, pairs);
        return sim;
    }

    @Override
    public int size() { return map == null ? 0 : map.size(); }

    @Override
    public Iterator<V> valueIterator() {
        return new ValueIterator<V>();
    }

    class KeyIterator<K> implements Iterator<K> {

        private Iterator<VMapEntry> iterator = map.iterator();

        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public K next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.key;
        }

        @Override
        public void remove() { iterator.remove(); }
    }

    class ValueIterator<V> implements Iterator<V> {

        private Iterator<VMapEntry> iterator = map.iterator();

        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public V next() {
            @SuppressWarnings("unchecked")
            VMapEntry<K, V> entry = iterator.next();
            return entry == null ? null : entry.getValue(versionSet);
        }

        @Override
        public void remove() { iterator.remove(); }
    }
}