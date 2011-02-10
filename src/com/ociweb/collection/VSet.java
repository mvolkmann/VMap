package com.ociweb.collection;

import java.util.Iterator;

public interface VSet<K> {

    VSet<K> add(K... values);

    VSet<K> clear();

    boolean contains(K value);

    VSet<K> delete(K... values);

    void dump();

    long getVersion();

    Iterator<K> iterator();

    int size();
}