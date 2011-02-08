package com.ociweb.collection;

import java.util.BitSet;

class VMapEntry {

    long hashCode;
    VersionValue firstVV;
    VMapEntry next;
    Object key;

    VMapEntry(Object key, VMapEntry next) {
        this.key = key;
        this.next = next;
        this.hashCode = key.hashCode();
    }

    void addValue(int version, Object value) {
        firstVV = new VersionValue(version, value, firstVV);
    }

    Object getValue(BitSet versionSet) {
        VersionValue vv = firstVV;
        while (vv != null) {
            if (versionSet.get(vv.version)) return vv.value;
            vv = vv.next;
        }

        return null;
    }

    @Override
    public String toString() {
        return key.toString() + '(' + hashCode +
            ")=" + firstVV.value;
    }
}