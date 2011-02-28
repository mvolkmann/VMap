package com.ociweb.collection;

/**
 * A map entry in a versioned, logically immutable map.
 * @param <K> the key type
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VMapEntry<K, V> {

    /**
     * Saved to facilitate rehashing.
     */
    long hashCode;

    /**
     * Each VMapEntry has a linked list of VersionValue objects.
     */
    VersionValue<V> firstVV;

    /**
     * The next entry within the same bucket.
     */
    VMapEntry<K, V> next;

    K key;

    /**
     * Creates an entry for a given key that has a given next entry.
     * The value is added later.
     * @param key the key
     * @param next the next entry
     */
    VMapEntry(K key, VMapEntry<K, V> next) {
        this.key = key;
        this.next = next;
        this.hashCode = key.hashCode();
    }

    /**
     * Adds a given value to a given version of this entry.
     * @param versionNumber the version number
     * @param value the value
     */
    void addValue(int versionNumber, V value) {
        firstVV = new VersionValue<V>(versionNumber, value, firstVV);
    }

    /**
     * Determines whether this entry contains a value for a given version.
     * @param version the version
     * @return true if so; false otherwise
     */
    boolean contains(Version version) {
        VersionValue vv = getVersionValue(version);
        return vv != null && vv.value != null;
    }

    /**
     * Gets the value of a given version in this entry.
     * @param version the version
     * @return the value or null if not found
     */
    V getValue(Version version) {
        VersionValue<V> vv = getVersionValue(version);
        return vv == null ? null : vv.value;
    }

    /**
     * Gets the VersionValue object for a given version in this entry.
     * @param version the Version
     * @return the VersionValue or null if not found
     */
    VersionValue<V> getVersionValue(Version version) {
        VersionValue<V> vv = firstVV;
        while (vv != null) {
            if (vv.version == version.number) return vv;
            if (vv.version < version.number &&
                version.ancestors.get(vv.version)) {
                return vv;
            }
            vv = vv.next;
        }

        return null;
    }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().
            append("  VMapEntry: key=").
            append(key).
            append(", hashCode=").
            append(hashCode);

        VersionValue vv = firstVV;
        while (vv != null) {
            sb.append("\n    ").append(vv);
            vv = vv.next;
        }

        return sb.toString();
    }
}