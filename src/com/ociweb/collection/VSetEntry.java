package com.ociweb.collection;

/**
 * A set entry in a versioned, logically immutable set.
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VSetEntry<V> {

    /**
     * Saved to facilitate rehashing.
     */
    long hashCode;

    /**
     * Each VSetEntry has a linked list of VersionMember objects.
     */
    VersionMember firstVM;

    /**
     * The next entry within the same bucket.
     */
    VSetEntry<V> next;

    V value;

    /**
     * Creates an entry for a given value that has a given next entry.
     * Whether the value is a member a given version is specified later.
     * @param value the value
     * @param next the next entry
     */
    VSetEntry(V value, VSetEntry<V> next) {
        this.value = value;
        this.next = next;
        this.hashCode = value.hashCode();
    }

    /**
     * Adds a VersionMember to this entry to indicate that this entry
     * either is or is not a member of a given version.
     * @param versionNumber the version number
     * @param member true if a member; false otherwise
     */
    void add(int versionNumber, boolean member) {
        firstVM = new VersionMember(versionNumber, member, firstVM);
    }

    /**
     * Determines whether this entry either is or is not
     * a member of a given version.
     * @param version the version
     * @param member true it verify it is a member;
     *               false to verify it is not a member
     * @return true if confirmed; false otherwise
     */
    boolean contains(Version version, boolean member) {
        VersionMember vm = getVersionMember(version);
        return vm != null && vm.member == member;
    }

    /**
     * Gets the VersionMember object for a given version in this entry.
     * @param version the Version
     * @return the VersionMember or null if not found
     */
    VersionMember getVersionMember(Version version) {
        VersionMember vm = firstVM;
        while (vm != null) {
            if (vm.version == version.number)  return vm;
            if (vm.version < version.number &&
                version.ancestors.get(vm.version)) {
                return vm;
            }
            vm = vm.next;
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
            append("  VSetEntry: value=").
            append(value).
            append(", hashCode=").
            append(hashCode);

        VersionMember vm = firstVM;
        while (vm != null) {
            sb.append("\n    ").append(vm);
            vm = vm.next;
        }

        return sb.toString();
    }
}