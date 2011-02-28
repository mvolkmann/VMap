package com.ociweb.collection;

/**
 * This is thrown when an attempt is made to
 * create too many versions of a VMap or VSet.
 * Since integers are used for version numbers,
 * the limit is Integer.MAX_VALUE.
 *
 * Do we really need to create 2.15 billon versions?
 * If so then version numbers could be stored in longs instead of ints.
 *
 * Note that more than one element can be added or deleted
 * in the same version.  Doing this would conserve version numbers.
 *
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionException extends RuntimeException {

    /**
     * Creates a VersionException.
     */
    VersionException() {
        super("attempted to create more than " +
            "Integer.MAX_VALUE versions of a VMap or VSet");
    }
}