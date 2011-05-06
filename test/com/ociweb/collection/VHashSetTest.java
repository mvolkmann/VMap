package com.ociweb.collection;

import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for the VHashSet class.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class VHashSetTest {

    private long getClojureSetPerformance(Collection<String> words) {
        long time0 = System.currentTimeMillis();

        IPersistentSet set = PersistentHashSet.EMPTY;
        // TODO: Why is the cast on the next line needed?
        // TODO: It seems the cons method is overloaded on return type!
        for (String word : words) set = (IPersistentSet) set.cons(word);
        long time1 = System.currentTimeMillis();

        for (String word : words) assertTrue(set.contains(word));
        long time2 = System.currentTimeMillis();

        report("Clojure PersistentHashSet", time0, time1, time2);
        return time2 - time0;
    }

    private long getJavaSetPerformance(Collection<String> words) {
        long time0 = System.currentTimeMillis();

        Set<String> mSet = new HashSet<String>();
        for (String word : words) mSet.add(word);
        long time1 = System.currentTimeMillis();

        for (String word : words) assertTrue(mSet.contains(word));
        long time2 = System.currentTimeMillis();

        report("Java HashSet", time0, time1, time2);
        return time2 - time0;
    }

    private long getVSetPerformance(Collection<String> words) {
        long time0 = System.currentTimeMillis();

        VSet<String> set = new VHashSet<String>(); //words.size());
        for (String word : words) set = set.add(word);
        long time1 = System.currentTimeMillis();

        for (String word : words) assertTrue(set.contains(word));
        long time2 = System.currentTimeMillis();

        report("VHashSet", time0, time1, time2);

        // Uncomment the next line to see statistics such as
        // max entry list length and rehash count.
        //set.dump("from getVSetPerformance", false);

        return time2 - time0;
    }

    private void report(String name, long t0, long t1, long t2) {
        System.out.println('\n' + name + " load = " + (t1 - t0) + "ms");
        System.out.println(name + " lookup = " + (t2 - t1) + "ms");
        System.out.println(name + " total = " + (t2 - t0) + "ms");
    }

    @Test
    public void testAddMultiple() {
        VSet<String> set = new VHashSet<String>("foo", "bar", "baz");
        assertEquals(3, set.size());
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
        assertTrue(set.contains("baz"));
        assertEquals(0, set.getVersionNumber());
    }

    @Test
    public void testAddMultiple2() {
        VSet<String> set = new VHashSet<String>("red", "orange", "yellow");
        assertEquals(0, set.getVersionNumber());
        set = set.add("green", "blue", "purple");
        assertEquals(1, set.getVersionNumber());
        assertTrue(set.contains("red"));
        assertTrue(set.contains("orange"));
        assertTrue(set.contains("yellow"));
        assertTrue(set.contains("green"));
        assertTrue(set.contains("blue"));
        assertTrue(set.contains("purple"));
    }

    @Test
    public void testAddSingle() {
        VSet<String> set0 = new VHashSet<String>();
        assertEquals(0, set0.getVersionNumber());

        VSet<String> set1 = set0.add("foo");
        assertEquals(set0.getVersionNumber() + 1, set1.getVersionNumber());

        // Same set is returned when an attempt to add a duplicate is made.
        VSet<String> set2 = set1.add("foo");
        assertEquals(set2, set1);

        // And again ...
        VSet<String> set3 = set1.add("foo");
        assertEquals(set3, set1);

        assertTrue(!set0.contains("foo"));
        assertTrue(set1.contains("foo"));
    }

    @Test
    public void testAddSingleMore() {
        String text = "Alice was beginning to get very tired";
        List<String> words =
            new ArrayList<String>(Arrays.asList(text.split(" ")));
        VSet<String> set = new VHashSet<String>();
        for (String word : words) set = set.add(word);
        for (String word : words) assertTrue(word, set.contains(word));
    }

    @Test
    public void testAddRepeat() {
        VSet<String> set0 = new VHashSet<String>();
        VSet<String> set1 = set0.add("foo", "bar");
        VSet<String> set2 = set1.delete("foo");
        VSet<String> set3 = set2.add("baz");

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
    public void testBranch() {
        VSet<String> set0 = new VHashSet<String>();
        VSet<String> set1 = set0.add("red");
        VSet<String> set2 = set1.add("orange");
        VSet<String> set3 = set2.add("yellow");
        VSet<String> set4 = set1.add("green");

        assertEquals(0, set0.getVersionNumber());
        assertEquals(1, set1.getVersionNumber());
        assertEquals(2, set2.getVersionNumber());
        assertEquals(3, set3.getVersionNumber());
        assertEquals(4, set4.getVersionNumber());

        assertEquals(0, set0.size());
        assertEquals(1, set1.size());
        assertEquals(2, set2.size());
        assertEquals(3, set3.size());
        assertEquals(2, set4.size());

        assertTrue(!set0.contains("red"));
        assertTrue(!set0.contains("orange"));
        assertTrue(!set0.contains("yellow"));
        assertTrue(!set0.contains("green"));

        assertTrue(set1.contains("red"));
        assertTrue(!set1.contains("orange"));
        assertTrue(!set1.contains("yellow"));
        assertTrue(!set1.contains("green"));

        assertTrue(set2.contains("red"));
        assertTrue(set2.contains("orange"));
        assertTrue(!set2.contains("yellow"));
        assertTrue(!set2.contains("green"));

        assertTrue(set3.contains("red"));
        assertTrue(set3.contains("orange"));
        assertTrue(set3.contains("yellow"));
        assertTrue(!set3.contains("green"));

        assertTrue(set4.contains("red"));
        assertTrue(!set4.contains("orange"));
        assertTrue(!set4.contains("yellow"));
        assertTrue(set4.contains("green"));
    }

    @Test
    public void testContains() {
        VSet<String> set = new VHashSet<String>("foo", "bar", "baz");
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
        assertTrue(set.contains("baz"));
        assertTrue(!set.contains("bad"));
    }

    @Test
    public void testDelete() {
        VSet<String> set0 = new VHashSet<String>();
        assertEquals(0, set0.getVersionNumber());

        VSet<String> set1 = set0.add("foo");
        assertEquals(set0.getVersionNumber() + 1, set1.getVersionNumber());

        VSet<String> set2 = set1.delete("foo");
        assertEquals(set1.getVersionNumber() + 1, set2.getVersionNumber());
        assertTrue(!set2.contains("foo"));

        // Same set is returned when an attempt
        // to delete a missing value is made.
        VSet<String> set3 = set2.delete("foo");
        assertEquals(set2, set3);

        // And again ...
        VSet<String> set4 = set2.delete("foo");
        assertEquals(set2, set4);

        // Re-add the deleted value.
        VSet<String> set5 = set2.add("foo");
        assertEquals(set2.getVersionNumber() + 1, set5.getVersionNumber());

        assertTrue(!set0.contains("foo"));
        assertTrue(set1.contains("foo"));
        assertTrue(!set2.contains("foo"));
        assertTrue(set5.contains("foo"));
    }

    @Test
    public void testDuplicateAdd() {
        VSet<String> set = new VHashSet<String>();
        assertEquals(0, set.size());
        set = set.add("red");
        assertEquals(1, set.size());
        set = set.add("orange");
        assertEquals(2, set.size());

        // Attempting to add an existing value returns the same set object
        // and doesn't increase the set size.
        VSet set2 = set.add("red");
        assertEquals(set, set2);
        assertEquals(2, set.size());

        assertTrue(set.contains("red"));
    }

    @Test
    public void testDuplicateAdd2() {
        VSet<String> set = new VHashSet<String>();
        set = set.add("red");
        set = set.add("orange");
        set = set.add("yellow");
        set = set.add("green");
        set = set.add("red");
        set = set.add("orange");
        set = set.add("red");
        set = set.add("red");
        // A sanity check that currently requires manual examination of output.
        //set.dump("from testDupicateAdd2", true);
    }

    @Test
    public void testIterator() {
        // Using a Java Set just for testing.
        String[] values = new String[] { "foo", "bar", "baz" };
        Set<String> valueSet = new HashSet<String>(Arrays.asList(values));

        VSet<String> set = new VHashSet<String>(values);

        Iterator<String> iter = set.iterator();

        while (!valueSet.isEmpty()) {
            assertTrue(iter.hasNext());
            String value = iter.next();
            assertTrue(valueSet.contains(value));
            valueSet.remove(value);
        }
    }

    @Test
    public void testIterator2() {
        VSet<String> set0 = new VHashSet<String>("red", "orange");
        VSet<String> set1 = set0.add("yellow");
        VSet<String> set2 = set1.add("green");
        VSet<String> set3 = set0.add("blue");

        assertEquals(4, set2.size());
        assertEquals(3, set3.size());

        // Using a Java Set just for testing.
        String[] values = new String[] { "red", "orange", "blue" };
        Set<String> valueSet = new HashSet<String>(Arrays.asList(values));

        Iterator<String> iter = set3.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(valueSet.contains(value));
            valueSet.remove(value);
        }
        assertTrue(valueSet.isEmpty());
    }

    @Test
    public void testPerformance() {
        Collection<String> words = TestData.getWords();
        System.out.println("word count = " + words.size());

        // Run each test twice to prime hotspot.

        System.out.println("\nPriming Runs:");
        long javaElapsed = getJavaSetPerformance(words);
        long clojureElapsed = getClojureSetPerformance(words);
        long vSetElapsed = getVSetPerformance(words);

        System.out.println("\nSecond Runs:");
        javaElapsed = getJavaSetPerformance(words);
        clojureElapsed = getClojureSetPerformance(words);
        vSetElapsed = getVSetPerformance(words);

        assertTrue("vSet < 3*HashSet", vSetElapsed < 3*javaElapsed);
        assertTrue("vSet < PersistentHashSet", vSetElapsed < clojureElapsed);
    }

    @Test
    public void testSize() {
        VSet<String> set = new VHashSet<String>("foo", "bar", "baz");
        assertEquals(3, set.size());
    }
}