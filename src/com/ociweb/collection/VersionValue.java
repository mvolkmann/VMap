package com.ociweb.collection;

/**
 * A versioned value in a VMap.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionValue<V> {

    final VersionValue<V> next;
    final V value;
    final int version;

    /**
     * Creates a VersionValue
     * @param version the version
     * @param value the value
     * @param next the next VersionValue in the linked list
     */
    VersionValue(int version, V value, VersionValue<V> next) {
        this.version = version;
        this.value = value;
        this.next = next;
    }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public String toString() {
        return "VersionValue: version=" + version + ", value=" + value;
    }
}