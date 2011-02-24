package com.ociweb.collection;

/**
 * A set entry in a versioned map.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <V> the value type
 */
class VSetEntry<V> {

    long hashCode;
    VersionMember firstVM;
    VSetEntry<V> next;
    V value;

    VSetEntry(V value, VSetEntry<V> next) {
        this.value = value;
        this.next = next;
        this.hashCode = value.hashCode();
    }

    void add(Version version, boolean member) {
        firstVM = new VersionMember(version.number, member, firstVM);
    }

    boolean contains(Version version) {
        VersionMember vm = getVersionMember(version);
        return vm != null && vm.member;
    }

    private VersionMember getVersionMember(Version version) {
        VersionMember vm = firstVM;
        while (vm != null) {
            if (vm.version == version.number) return vm;
            if (vm.version < version.number &&
                version.ancestors.get(vm.version)) {
                return vm;
            }
            vm = vm.next;
        }
        return null;
    }

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