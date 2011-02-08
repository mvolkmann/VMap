package com.ociweb.collection;

import java.util.BitSet;
import java.util.Iterator;

public class VHashMap implements VMap {

    private BitSet versionSet;
    private InternalMap map;
    private int version;

    public VHashMap() {}

    public VHashMap(Tuple... tuples) {
        map = new InternalMap(tuples.length);
        map.put(version, tuples);
        versionSet = new BitSet(1);
        versionSet.set(version); // zero
    }

    @Override
    public VMap clear() {
        return new VHashMap();
    }

    @Override
    public boolean containsKey(Object key) {
        return map == null ? false : map.contains(versionSet, key);
    }

    private synchronized VHashMap copy() {
        VHashMap sim = new VHashMap();
        sim.map = map == null ? new InternalMap() : map;
        sim.version = version + 1;
        sim.versionSet = new BitSet(sim.version);
        if (versionSet != null) sim.versionSet.or(versionSet);
        sim.versionSet.set(sim.version);
        return sim;
    }

    @Override
    public VMap delete(Object... keys) {
        VHashMap sim = copy();
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
    public Object get(Object key) {
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
    public Iterator<Object> keyIterator() {
        return new SimpleImmutableMapIterator(true);
    }

    @Override
    public VMap put(Object key, Object value) {
        VHashMap sim = copy();
        sim.map.put(sim.version, key, value);
        return sim;
    }

    @Override
    public VMap put(Tuple... tuples) {
        VHashMap sim = copy();
        sim.map.put(sim.version, tuples);
        return sim;
    }

    @Override
    public int size() { return map == null ? 0 : map.size(); }

    @Override
    public Iterator<Object> valueIterator() {
        return new SimpleImmutableMapIterator(false);
    }

    class SimpleImmutableMapIterator implements Iterator {

        private Iterator iterator;
        private boolean wantKeys;

        SimpleImmutableMapIterator(boolean wantKeys) {
            iterator = map.iterator();
            this.wantKeys = wantKeys;
        }

        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public Object next() {
            VMapEntry entry = (VMapEntry) iterator.next();
            return entry == null ? null :
                wantKeys ? entry.key : entry.getValue(versionSet);
        }

        @Override
        public void remove() { iterator.remove(); }
    }
}