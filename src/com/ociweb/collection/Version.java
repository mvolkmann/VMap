package com.ociweb.collection;

import com.ociweb.lang.MutableInteger;
import java.util.BitSet;

/**
 * Holds version information for a VMap or VSet.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class Version {

    // Note that java.util.BitSet grows as needed in increments of 64 bits.
    // It stores its value in an array of longs.
    BitSet ancestors;
    int number; // TODO: Use long instead of int?

    Version() {
        number = 0;
        ancestors = new BitSet(1);
        ancestors.set(number);
    }

    Version(MutableInteger highest, Version parent) {
        if (parent.number == highest.value) {
            ancestors = parent.ancestors;
        } else {
            ancestors = (BitSet) parent.ancestors.clone();

            // Clear bits above current version.
            ancestors.clear(parent.number + 1, ancestors.size());
        }

        number = ++highest.value;
        ancestors.set(number);
    }

    @Override
    public String toString() {
        String s = "version: " + number + ", ancestors:";
        for (int i = 0; i <= number; i++) {
            if (ancestors.get(i)) s += " " + i;
        }
        return s;
    }
}