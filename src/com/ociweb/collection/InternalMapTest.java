package com.ociweb.collection;

import org.junit.*;

import static org.junit.Assert.*;

public class InternalMapTest {

    private void log(Object obj) {
        System.out.println(obj);
    }

    /**
     * This test just verifiers that the rehash method
     * doesn't get into an infinite loop.
     */
    @Test
    public void testRehash() {
        InternalMap<String, Integer> map = new InternalMap<String, Integer>();
        int version = 0;
        map.put(version, "foo", 1);
        map.put(version, "bar", 2);
        map.put(version, "baz", 3);
        map.rehash();
        assertTrue(true);
    }
}