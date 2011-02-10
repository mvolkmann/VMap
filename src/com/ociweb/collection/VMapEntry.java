package com.ociweb.collection;

import java.util.BitSet;

class VMapEntry<K, V> {

    long hashCode;
    VersionValue<V> firstVV;
    VMapEntry<K, V> next;
    K key;

    VMapEntry(K key, VMapEntry<K, V> next) {
        this.key = key;
        this.next = next;
        this.hashCode = key.hashCode();
    }

    void addValue(int version, V value) {
        firstVV = new VersionValue<V>(version, value, firstVV);
    }

    V getValue(BitSet versionSet) {
        VersionValue<V> vv = firstVV;
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