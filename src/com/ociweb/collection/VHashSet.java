package com.ociweb.collection;

import java.util.BitSet;
import java.util.Iterator;

public class VHashSet implements VSet {

    private BitSet versionSet;
    private InternalMap map;
    private int version;

    public VHashSet() {}

    public VHashSet(Object... values) {
        map = new InternalMap(values.length);

        for (Object value : values) {
            log("ctor", "value", value);
            map.put(version, value, true);
        }

        versionSet = new BitSet(1);
        versionSet.set(version); // zero
    }

    @Override
    public final VSet add(Object... values) {
        VHashSet sis = copy();
        for (Object value : values) {
            sis.map.put(sis.version, value, true);
        }
        return sis;
    }

    @Override
    public VHashSet clear() {
        return new VHashSet();
    }

    @Override
    public boolean contains(Object value) {
        if (map == null) return false;
        Object found = map.get(versionSet, value);
        return found == null ? false : (Boolean) found;
    }

    private synchronized VHashSet copy() {
        VHashSet sis = new VHashSet();
        sis.map = map == null ? new InternalMap() : map;
        sis.version = version + 1;
        sis.versionSet = new BitSet(sis.version);
        if (versionSet != null) sis.versionSet.or(versionSet);
        sis.versionSet.set(sis.version);
        return sis;
    }

    @Override
    public VSet delete(Object... values) {
        VHashSet sis = copy();
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
    public Iterator<Object> iterator() {
        return new SimpleImmutableSetIterator();
    }

    private void log(String method, Object msg) {
        //System.out.println("SimpleImmutableSet." + method + ": " + msg);
    }

    private void log(String method, String name, Object value) {
        log(method, name + " = " + value);
    }

    @Override
    public int size() { return map == null ? 0 : map.size(); }

    class SimpleImmutableSetIterator implements Iterator {

        private Iterator iterator;

        SimpleImmutableSetIterator() { iterator = map.iterator(); }

        @Override
        public boolean hasNext() { return iterator.hasNext(); }

        @Override
        public Object next() {
            VMapEntry entry = (VMapEntry) iterator.next();
            return entry == null ? null : entry.key;
        }

        @Override
        public void remove() { iterator.remove(); }
    }
}