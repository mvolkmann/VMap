package com.ociweb.collection;

import java.util.Iterator;

public interface VMap<K, V> {

    VMap<K, V> clear();

    boolean containsKey(K key);

    VMap<K, V> delete(K... keys);

    void dump();

    V get(K key);

    long getVersion();

    Iterator<K> keyIterator();

    VMap<K, V>  put(K key, V value);

    VMap<K, V> put(Pair<K, V>... pairs);

    int size();

    Iterator<V> valueIterator();
}
