package com.ociweb.collection;

/**
 * A versioned value in a VMap.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionValue<V> {

    final VersionValue<V> next;
    final V value;
    final int version;

    VersionValue(int version, V value, VersionValue<V> next) {
        this.version = version;
        this.value = value;
        this.next = next;
    }

    @Override
    public String toString() {
        return "VersionValue: version=" + version + ", value=" + value;
    }
}