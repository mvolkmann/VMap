package com.ociweb.collection;

import com.ociweb.lang.MutableInteger;
import java.util.BitSet;

/**
 * Holds version information for a VMap or VSet.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class Version {

    // Note that a java.util.BitSet grows as needed in increments of 64 bits.
    // It stores its value in an array of longs.
    BitSet ancestors;
    int number; // TODO: Use long instead of int?

    /**
     * Creates a version zero.
     */
    Version() {
        number = 0;
        ancestors = new BitSet(1);
        ancestors.set(number);
    }

    /**
     * Creates the next available version, based on a parent version.
     * @param highest
     * @param parent the parent version
     */
    Version(MutableInteger highest, Version parent) {
        // If the parent has the highest version created so far ...
        if (parent.number == highest.value) {
            // This version can share the ancestors BitSet of its parent.
            ancestors = parent.ancestors;
        } else {
            // Copy the ancestors BitSet of its parent.
            ancestors = (BitSet) parent.ancestors.clone();

            // Clear the bits above current version because
            // they represent versions that are not ancestors of this version.
            ancestors.clear(parent.number + 1, ancestors.size());
        }

        // This version number is one higher than the highest used so far.
        number = ++highest.value;

        // Set the ancestor bit for this version number.
        ancestors.set(number);
    }

    /**
     * Gets the string representation of this object.
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("version: ").append(number).append(", ancestors:");

        for (int i = 0; i <= number; i++) {
            if (ancestors.get(i)) sb.append(" ").append(i);
        }

        return sb.toString();
    }
}