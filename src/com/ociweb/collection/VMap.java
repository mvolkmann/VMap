package com.ociweb.collection;

import java.util.Iterator;

/**
 * Interface to a versioned, immutable map that provides methods for
 * efficiently creating logical copies of the map.
 * @author R. Mark Volkmann, Object Computing, Inc.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface VMap<K, V> {

    /**
     * Determines whether a given key is in this map.
     * @param key the key
     * @return true if a member; false otherwise
     */
    boolean containsKey(K key);

    /**
     * Creates a new map containing the entries in this map
     * minus entries for one or more given keys.
     * @param keys the keys
     * @return the new map
     */
    VMap<K, V> delete(K... keys);

    /**
     * Dumps the contents of this map to stdout
     * in a form that is useful for debugging.
     * @param name to identify in debuggging output
     */
    void dump(String name);

    /**
     * Gets the value that corresponds to a given key in this map.
     * @param key the key
     * @return the value
     */
    V get(K key);

    /**
     * Gets the version number of this map.
     * This is mainly useful for debugging.
     * @return the version number
     */
    int getVersionNumber();

    /**
     * Gets an Iterator for iterating through the keys of this map.
     * @return the iterator
     */
    Iterator<K> keyIterator();

    /**
     * Creates a new map containing the entries in this map
     * and one new entry.
     * Note that putting a null value is the same as deleting.
     * @param key the key of the new entry
     * @param value the value of the new entry
     * @return the new map
     */
    VMap<K, V> put(K key, V value);

    /**
     * Creates a new map containing the entries in this map
     * and one or more new entries.
     * Note that putting null values is the same as deleting.
     * @param pairs one or more Pair objects that each hold a key and a value
     * @return the new map
     */
    VMap<K, V> put(Pair<K, V>... pairs);

    /**
     * Gets the number of entries in this map.
     * @return the number of entries
     */
    int size();

    /**
     * Gets an Iterator for iterating through the values of this map.
     * @return the iterator
     */
    Iterator<V> valueIterator();
}
