package com.ociweb.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.junit.*;

import static org.junit.Assert.*;

public class VHashSetTest {

    private void log(Object obj) {
        System.out.println(obj);
    }

    @Test
    public void testAddMultiple() {
        VSet set = new VHashSet("foo", "bar", "baz");
        assertEquals(3, set.size());
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
        assertTrue(set.contains("baz"));
    }

    @Test
    public void testAddSingle() {
        VSet set0 = new VHashSet();
        VSet set1 = set0.add("foo");
        assertTrue(set1.getVersion() == set0.getVersion() + 1);

        assertTrue(!set0.contains("foo"));
        assertTrue(set1.contains("foo"));
    }

    @Test
    public void testAddSingleMore() {
        String text = "Alice was beginning to get very tired";
        List<String> words =
            new ArrayList<String>(Arrays.asList(text.split(" ")));
        VSet set = new VHashSet();
        for (String word : words) {
            set = set.add(word);
            //System.out.println();
            //assertTrue(word, set.contains(word));
            //System.out.println("---");
        }

        //set.dump();

        //System.out.println("===");
        for (String word : words) {
            assertTrue(word, set.contains(word));
            //System.out.println("---");
        }
    }

    @Test
    public void testAddRepeat() {
        VSet set0 = new VHashSet();
        VSet set1 = set0.add("foo", "bar");
        VSet set2 = set1.delete("foo");
        VSet set3 = set2.add("baz");

        assertTrue(!set0.contains("foo"));
        assertTrue(!set0.contains("bar"));
        assertTrue(!set0.contains("baz"));

        assertTrue(set1.contains("foo"));
        assertTrue(set1.contains("bar"));
        assertTrue(!set1.contains("baz"));

        assertTrue(!set2.contains("foo"));
        assertTrue(set2.contains("bar"));
        assertTrue(!set2.contains("baz"));

        assertTrue(!set3.contains("foo"));
        assertTrue(set3.contains("bar"));
        assertTrue(set3.contains("baz"));
    }

    @Test
    public void testClear() {
        VSet set0 = new VHashSet("foo", "bar", "baz");
        assertEquals(3, set0.size());
        VSet set1 = set0.clear();
        assertEquals(0, set1.size());
    }

    @Test
    public void testContains() {
        VSet set = new VHashSet("foo", "bar", "baz");
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
        assertTrue(set.contains("baz"));
        assertTrue(!set.contains("bad"));
    }

    @Test
    public void testDelete() {
        VSet set0 = new VHashSet();
        VSet set1 = set0.add("foo");
        assertTrue(set1.getVersion() == set0.getVersion() + 1);

        VSet set2 = set1.delete("foo");
        assertTrue(set2.getVersion() == set1.getVersion() + 1);

        assertTrue(!set0.contains("foo"));
        assertTrue(set1.contains("foo"));
        assertTrue(!set2.contains("foo"));
    }

    @Test
    public void testIterator() {
        // Using a Java Set just for testing.
        Object[] values = new String[] { "foo", "bar", "baz" };
        Set valueSet = new HashSet(Arrays.asList(values));

        VSet set = new VHashSet(values);

        Iterator iter = set.iterator();

        while (!valueSet.isEmpty()) {
            assertTrue(iter.hasNext());
            Object value = iter.next();
            assertTrue(valueSet.contains(value));
            valueSet.remove(value);
        }
    }

    @Test
    public void testPerformance() {
        List<String> words = Profile.getWords();
        log("words size = " + words.size()); // 5946

        //---------------------------------------------------------------------
        // Test standard Java set.
        //---------------------------------------------------------------------
        long startTime = System.currentTimeMillis();
        Set mSet = new HashSet();
        for (String word : words) mSet.add(word);
        for (String word : words) assertTrue(mSet.contains(word));
        long mSetElapsed = System.currentTimeMillis() - startTime;

        //---------------------------------------------------------------------
        // Test immutable set.
        //---------------------------------------------------------------------
        startTime = System.currentTimeMillis();
        VSet iSet = new VHashSet();
        for (String word : words) iSet = iSet.add(word);
        for (String word : words) assertTrue(word, iSet.contains(word));
        long iSetElapsed = System.currentTimeMillis() - startTime;

        //---------------------------------------------------------------------
        // Compare their performance.
        //---------------------------------------------------------------------
        String msg =
            "immutable set performance (" + iSetElapsed + ") < " +
            "triple mutable set performance (" + mSetElapsed + ')';
        log(msg);
        assertTrue(msg, iSetElapsed < 3*mSetElapsed);
    }

    @Test
    public void testSize() {
        VSet set = new VHashSet("foo", "bar", "baz");
        assertEquals(3, set.size());
    }
}