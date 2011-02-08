package com.ociweb.collection;

import java.util.Iterator;

public interface VMap {

    VMap clear();

    boolean containsKey(Object key);

    VMap delete(Object... keys);

    void dump();

    Object get(Object key);

    long getVersion();

    Iterator<Object> keyIterator();

    VMap put(Object key, Object value);

    VMap put(Tuple... tuples);

    int size();

    Iterator<Object> valueIterator();
}
