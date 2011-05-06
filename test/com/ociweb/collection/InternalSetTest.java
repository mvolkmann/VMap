package com.ociweb.collection;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for the InternalSet class.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class InternalSetTest {

    /**
     * This test just verifies that the rehash method
     * doesn't get into an infinite loop.
     */
    @Test
    public void testRehash() {
        InternalSet<String> set = new InternalSet<String>();

        Version version = new Version();
        boolean added = set.add(version, "foo");
        assertTrue(added);
        added = set.add(version, "bar");
        assertTrue(added);
        added = set.add(version, "baz");
        assertTrue(added);

        set.rehash();
        assertTrue(true);
    }
}