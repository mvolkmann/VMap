package com.ociweb.collection;

/**
 * A versioned member in a VSet.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionMember {

    final VersionMember next;
    final boolean member;
    final int version;

    /**
     * Creates a VersionMember
     * @param version the version
     * @param member true if a member; false otherwise
     * @param next the next VersionMember in the linked list
     */
    VersionMember(int version, boolean member, VersionMember next) {
        this.version = version;
        this.member = member;
        this.next = next;
    }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public String toString() {
        return "VersionMember: version=" + version + ", member=" + member;
    }
}