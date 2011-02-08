package com.ociweb.collection;

class VersionValue {

    final int version;
    final VersionValue next;
    final Object value;

    VersionValue(int version, VersionValue next) {
        this.version = version;
        this.value = null;
        this.next = next;
    }

    VersionValue(int version, Object value, VersionValue next) {
        this.version = version;
        this.value = value;
        this.next = next;
        //System.out.println("VersionValue.ctor: " +
        //    "version=" + version + ", value=" + value);
    }
}