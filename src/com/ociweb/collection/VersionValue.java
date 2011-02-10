package com.ociweb.collection;

class VersionValue<V> {

    final int version;
    final VersionValue<V> next;
    final V value;

    VersionValue(int version, VersionValue<V> next) {
        this.version = version;
        this.value = null;
        this.next = next;
    }

    VersionValue(int version, V value, VersionValue<V> next) {
        this.version = version;
        this.value = value;
        this.next = next;
        //System.out.println("VersionValue.ctor: " +
        //    "version=" + version + ", value=" + value);
    }
}