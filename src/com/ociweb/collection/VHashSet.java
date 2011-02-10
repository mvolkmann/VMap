package com.ociweb.collection;

import java.util.BitSet;
import java.util.Iterator;

public class VHashSet<K> implements VSet<K> {

    private BitSet versionSet;
    private InternalMap<K, Boolean> map;
    private int version;

    public VHashSet() {}

    public VHashSet(K... values) {
        map = new InternalMap<K, Boolean>(values.length);

        for (K value : values) {
            log("ctor", "value", value);
            map.put(version, value, true);
        }

        versionSet = new BitSet(1);
        versionSet.set(version); // zero
    }

    @Override
    public final VSet<K> add(K... values) {
        VHashSet<K> sis = copy();
        for (K value : values) {
            sis.map.put(sis.version, value, true);
        }
        return sis;
    }

    @Override
    public VHashSet<K> clear() {
        return new VHashSet<K>();
    }

    @Override
    public boolean contains(K value) {
        if (map == null) return false;
        Boolean found = map.get(versionSet, value);
        return found == null ? false : found;
    }

    private synchronized VHashSet<K> copy() {
        VHashSet<K> sis = new VHashSet<K>();
        sis.map = map == null ? new InternalMap<K, Boolean>() : map;
        sis.version = version + 1;
        sis.versionSet = new BitSet(sis.version);
        if (versionSet != null) sis.versionSet.or(versionSet);
        sis.versionSet.set(sis.version);
        return sis;
    }

    @Override
    public VSet<K> delete(K... values) {
        VHashSet<K> sis = copy();
        sis.map.delete(sis.version, values);
        return sis;
    }

    @Override
    public void dump() {
        System.out.println("Set Dump:");
        if (map == null) {
            System.out.println("  empty");
        } else {
            map.dump();
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Next line makes NetBeans happy.
        if (!(obj instanceof VHashSet)) return false;
        return obj == this;
    }

    @Override
    public long getVersion() { return version; }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
            "can't use as a key in a map or set");
    }

    @Override
    public Iterator<K> iterator() {
        return new VHashSetIterator<K, Boolean>();
    }

    private void log(String method, Object msg) {
        //System.out.println("VHashSet." + method + ": " + msg);
    }

    private void log(String method, String name, Object value) {
        log(method, name + " = " + value);
    }

    @Override
    public int size() { return map == null ? 0 : map.size(); }

    class VHashSetIterator<K, V> implements Iterator<K> {

        private Iterator<VMapEntry> iterator;

        VHashSetIterator() { iterator = map.iterator(); }

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
}