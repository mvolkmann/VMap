package com.ociweb.collection;

/**
 * This is thrown when an attempt is made to
 * create too many versions of a VMap or VSet.
 * Since integers are used for version numbers,
 * the limit is Integer.MAX_VALUE.
 *
 * Do you really need to create 2.15 billon versions?
 *
 * Note that more than one element can be added or deleted
 * in the same version.
 *
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class VersionException extends RuntimeException {

    VersionException() {
        super("attempted to create more than " +
            "Integer.MAX_VALUE versions of a VMap or VSet");
    }
}