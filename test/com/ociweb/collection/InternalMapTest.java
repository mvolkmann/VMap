package com.ociweb.collection;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for the InternalMap class.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class InternalMapTest {

    /**
     * This test just verifies that the rehash method
     * doesn't get into an infinite loop.
     */
    @Test
    public void testRehash() {
        InternalMap<String, Integer> map = new InternalMap<String, Integer>();
        Version version = new Version();

        InternalMap.PutAction putAction = map.put(version, "foo", 1);
        assertTrue(putAction == InternalMap.PutAction.ADDED_ENTRY);
        putAction = map.put(version, "bar", 2);
        assertTrue(putAction == InternalMap.PutAction.ADDED_ENTRY);
        putAction = map.put(version, "baz", 3);
        assertTrue(putAction == InternalMap.PutAction.ADDED_ENTRY);

        map.rehash();
        assertTrue(true);
    }
}