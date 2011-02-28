package com.ociweb.collection;

/**
 * Holds a key/value pair.
 * @param <K> the key type
 * @param <V> the value type
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class Pair<K, V> {
    public final K key;
    public final V value;

    /**
     * Creates a Pair object.
     * @param key the key
     * @param value the value
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}