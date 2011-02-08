package com.ociweb.collection;

import java.util.Iterator;

public interface VSet {

    VSet add(Object... values);

    VSet clear();

    boolean contains(Object value);

    VSet delete(Object... values);

    void dump();

    long getVersion();

    Iterator<Object> iterator();

    int size();
}
