package com.ociweb.collection;

/**
 * A map entry in a versioned map.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <K> the key type
 * @param <V> the value type
 */
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

    void addValue(Version version, V value) {
        firstVV = new VersionValue<V>(version.number, value, firstVV);
    }

    boolean contains(Version version) {
        VersionValue vv = getVersionValue(version);
        return vv != null;
    }

    V getValue(Version version) {
        VersionValue<V> vv = getVersionValue(version);
        return vv == null ? null : vv.value;
    }

    private VersionValue<V> getVersionValue(Version version) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().
            append("  VMapEntry: key=").
            append(key).
            append(", hashCode=").
            append(hashCode);

        VersionValue vv = firstVV;
        while (vv != null) {
            sb.append("\n    ").append(vv.version);
            vv = vv.next;
        }

        return sb.toString();
    }
}