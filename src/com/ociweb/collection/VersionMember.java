package com.ociweb.collection;

/**
 * A versioned member in a VSet.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionMember {

    final VersionMember next;
    final boolean member;
    final int version;

    VersionMember(int version, boolean member, VersionMember next) {
        this.version = version;
        this.member = member;
        this.next = next;
    }

    @Override
    public String toString() {
        return "VersionMember: version=" + version + ", member=" + member;
    }
}