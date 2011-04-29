package com.ociweb.collection;

import java.util.Iterator;

/**
 * Interface to a versioned, logically immutable set
 * that provides methods for efficiently creating logical copies of the set.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <V> the value type
 */
public interface VSet<V> {

    /**
     * Creates a new set containing the values in this set
     * and one or more new values.
     * @param values the new values
     * @return the new set
     */
    VSet<V> add(V... values);

    /**
     * Determines whether a given value is a member of this set.
     * @param value the value
     * @return true if a member; false otherwise
     */
    boolean contains(V value);

    /**
     * Creates a new set containing the values in this set
     * minus one or more given values.
     * @param values the values
     * @return the new set
     */
    VSet<V> delete(V... values);

    /**
     * Dumps the contents of this set to stdout
     * in a form that is useful for debugging.
     * @param name to identify in debugging output
     * @param detail true to include content; false for only high-level
     */
    void dump(String name, boolean includeContent);

    /**
     * Gets the version number of this set.
     * This is mainly useful for debugging.
     * @return the version number
     */
    int getVersionNumber();

    /**
     * Gets an iterator for iterating through the members of this set.
     * @return the iterator
     */
    Iterator<V> iterator();

    /**
     * Gets the number of members in this set.
     * @return the number of members
     */
    int size();
}