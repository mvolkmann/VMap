package com.ociweb.collection;

import java.util.List;
import java.util.Collection;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentHashMap;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for the VHashMap class.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class VHashMapTest {

    private long getClojureMapPerformance(
        Collection<String> words, List<Pair<String, String>> pairs) {

        long time0 = System.currentTimeMillis();
        IPersistentMap map = PersistentHashMap.EMPTY;
        String prevWord = TestData.getFirstKey();
        for (String word : words) {
            map = map.assoc(prevWord, word);
            prevWord = word;
        }
        long time1 = System.currentTimeMillis();

        /*
        prevWord = TestData.getFirstKey();
        for (String word : words) {
            assertEquals(word, map.valAt(prevWord));
            prevWord = word;
        }
        */
        for (Pair<String, String> pair : pairs) {
            assertEquals(pair.value, map.valAt(pair.key));
        }
        long time2 = System.currentTimeMillis();

        report("Clojure PersistenHashMap", time0, time1, time2);
        return time2 - time0;
    }

    private long getJavaMapPerformance(
        Collection<String> words, List<Pair<String, String>> pairs) {

        long time0 = System.currentTimeMillis();
        Map<String, String> map = new HashMap<String, String>();
        String prevWord = TestData.getFirstKey();
        for (String word : words) {
            map.put(prevWord, word);
            prevWord = word;
        }
        long time1 = System.currentTimeMillis();

        /*
        prevWord = TestData.getFirstKey();
        for (String word : words) {
            assertEquals(word, map.get(prevWord));
            prevWord = word;
        }
        */
        for (Pair<String, String> pair : pairs) {
            assertEquals(pair.value, map.get(pair.key));
        }
        long time2 = System.currentTimeMillis();

        report("Java HashMap", time0, time1, time2);
        return time2 - time0;
    }

    private long getVMapPerformance(
        Collection<String> words, List<Pair<String, String>> pairs) {

        long time0 = System.currentTimeMillis();
        VMap<String, String> map = new VHashMap<String, String>();
        String prevWord = TestData.getFirstKey();
        for (String word : words) {
            map = map.put(prevWord, word);
            prevWord = word;
        }
        long time1 = System.currentTimeMillis();

        /*
        prevWord = TestData.getFirstKey();
        for (String word : words) {
            assertEquals(word, map.get(prevWord));
            prevWord = word;
        }
        */
        for (Pair<String, String> pair : pairs) {
            assertEquals(pair.value, map.get(pair.key));
        }
        long time2 = System.currentTimeMillis();

        report("VHashMap", time0, time1, time2);

        // Uncomment the next line to see statistics such as
        // max entry list length and rehash count.
        map.dump("from getVMapPerformance", false);

        return time2 - time0;
    }

    private Pair<String, Integer>[] makePairs() {
        @SuppressWarnings("unchecked")
        Pair<String, Integer>[] pairs = (Pair<String, Integer>[])
            Array.newInstance(Pair.class, 3);

        pairs[0] = new Pair<String, Integer>("foo", 1);
        pairs[1] = new Pair<String, Integer>("bar", 2);
        pairs[2] = new Pair<String, Integer>("baz", 3);

        return pairs;
    }

    private void report(String name, long t0, long t1, long t2) {
        System.out.println('\n' + name + " load = " + (t1 - t0) + "ms");
        System.out.println(name + " lookup = " + (t2 - t1) + "ms");
        System.out.println(name + " total = " + (t2 - t0) + "ms");
    }

    @Test
    public void testBranch() {
        VMap<String, Integer> map0 = new VHashMap<String, Integer>();
        VMap<String, Integer> map1 = map0.put("red", 1);
        VMap<String, Integer> map2 = map1.put("orange", 2);
        VMap<String, Integer> map3 = map2.put("yellow", 3);
        VMap<String, Integer> map4 = map1.put("green", 4);

        assertEquals(0, map0.getVersionNumber());
        assertEquals(1, map1.getVersionNumber());
        assertEquals(2, map2.getVersionNumber());
        assertEquals(3, map3.getVersionNumber());
        assertEquals(4, map4.getVersionNumber());

        assertEquals(0, map0.size());
        assertEquals(1, map1.size());
        assertEquals(2, map2.size());
        assertEquals(3, map3.size());
        assertEquals(2, map4.size());

        assertTrue(!map0.containsKey("red"));
        assertTrue(!map0.containsKey("orange"));
        assertTrue(!map0.containsKey("yellow"));
        assertTrue(!map0.containsKey("green"));

        assertTrue(map1.containsKey("red"));
        assertTrue(!map1.containsKey("orange"));
        assertTrue(!map1.containsKey("yellow"));
        assertTrue(!map1.containsKey("green"));

        assertTrue(map2.containsKey("red"));
        assertTrue(map2.containsKey("orange"));
        assertTrue(!map2.containsKey("yellow"));
        assertTrue(!map2.containsKey("green"));

        assertTrue(map3.containsKey("red"));
        assertTrue(map3.containsKey("orange"));
        assertTrue(map3.containsKey("yellow"));
        assertTrue(!map3.containsKey("green"));

        assertTrue(map4.containsKey("red"));
        assertTrue(!map4.containsKey("orange"));
        assertTrue(!map4.containsKey("yellow"));
        assertTrue(map4.containsKey("green"));
    }

    @Test
    public void testContains() {
        VMap<String, Integer> map = new VHashMap<String, Integer>(makePairs());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(map.containsKey("baz"));
        assertTrue(!map.containsKey("bad"));
    }

    @Test
    public void testDelete() {
        VMap<String, String> map0 = new VHashMap<String, String>();

        // It shouldn't be an error to try to delete a non-existent key,
        // but it does create a new version.
        VMap<String, String> map1 = map0.delete("foo");

        VMap<String, String> map2 = map1.put("foo", "bar");
        assertEquals(map1.getVersionNumber() + 1, map2.getVersionNumber());

        VMap<String, String> map3 = map2.delete("foo");
        assertEquals(map2.getVersionNumber() + 1, map3.getVersionNumber());

        assertNull(map0.get("foo"));
        assertEquals("bar", map2.get("foo"));
        assertNull(map3.get("foo"));
    }

    @Test
    public void testDuplicatePut() {
        VMap<String, Integer> map = new VHashMap<String, Integer>();
        assertEquals(0, map.size());

        map = map.put("red", 1);
        assertEquals(1, map.size());

        map = map.put("orange", 2);
        assertEquals(2, map.size());

        // A put with an existing key doesn't increase the map size.
        map = map.put("red", 3);
        assertEquals(2, map.size());

        // Latest put for a given key replaces the previous value.
        assertEquals(3, (int) map.get("red"));
    }

    @Test
    public void testKeyIterator() {
        // Using a Java Set just for testing.
        String[] keys = new String[] { "foo", "bar", "baz" };
        Set<String> keySet = new HashSet<String>(Arrays.asList(keys));

        VMap<String, Integer> map = new VHashMap<String, Integer>(makePairs());
        Iterator<String> iter = map.keyIterator();
        while (!keySet.isEmpty()) {
            assertTrue(iter.hasNext());
            String key = iter.next();
            assertTrue(keySet.contains(key));
            keySet.remove(key);
        }
    }

    @Test
    public void testKeyIterator2() {
        VMap<String, Integer> map0 = new VHashMap<String, Integer>();
        map0 = map0.put("red", 1);
        map0 = map0.put("orange", 2);
        VMap<String, Integer> map1 = map0.put("yellow", 3);
        VMap<String, Integer> map2 = map1.put("green", 4);
        VMap<String, Integer> map3 = map0.put("blue", 5);

        assertEquals(4, map2.size());
        assertEquals(3, map3.size());

        // Using a Java Set just for testing.
        String[] values = new String[] { "red", "orange", "blue" };
        Set<String> valueMap = new HashSet<String>(Arrays.asList(values));

        Iterator<String> iter = map3.keyIterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(valueMap.contains(value));
            valueMap.remove(value);
        }
        assertTrue(valueMap.isEmpty());
    }

    @Test
    public void testPerformance() {
        //Collection<String> words = TestData.getUniqueWords();
        Collection<String> words = TestData.getWords();
        List<Pair<String, String>> pairs = TestData.getPairs();

        System.out.println("word count = " + words.size());

        // Run each test twice to prime hotspot.

        System.out.println("\nPriming Runs:");
        long javaElapsed = getJavaMapPerformance(words, pairs);
        long clojureElapsed = getClojureMapPerformance(words, pairs);
        long vMapElapsed = getVMapPerformance(words, pairs);

        System.out.println("\nSecond Runs:");
        javaElapsed = getJavaMapPerformance(words, pairs);
        clojureElapsed = getClojureMapPerformance(words, pairs);
        vMapElapsed = getVMapPerformance(words, pairs);

        assertTrue("vMap < 3*HashMap", vMapElapsed < 3*javaElapsed);
        assertTrue("vMap < PersistentHashMap", vMapElapsed < clojureElapsed);
    }

    @Test
    public void testPutSingle() {
        VMap<String, String> map0 = new VHashMap<String, String>();
        VMap<String, String> map1 = map0.put("foo", "bar");
        assertEquals(map0.getVersionNumber() + 1, map1.getVersionNumber());

        assertNull(map0.get("foo"));
        assertEquals("bar", map1.get("foo"));
    }

    @Test
    public void testPutMultiple() {
        VMap<String, Integer> map0 = new VHashMap<String, Integer>();
        VMap<String, Integer> map1 = map0.put(makePairs());
        assertEquals(3, map1.size());
        assertEquals(1, (int) map1.get("foo"));
        assertEquals(2, (int) map1.get("bar"));
        assertEquals(3, (int) map1.get("baz"));
    }

    @Test
    public void testPutRepeat() {
        VMap<String, Integer> map0 = new VHashMap<String, Integer>();
        VMap<String, Integer> map1 = map0.put("foo", 1);
        VMap<String, Integer> map2 = map1.put("bar", 2);
        VMap<String, Integer> map3 = map2.delete("foo");
        VMap<String, Integer> map4 = map3.put("bar", 3);
        VMap<String, Integer> map5 = map4.put("foo", 4);

        assertTrue(!map0.containsKey("foo"));
        assertTrue(!map0.containsKey("bar"));
        assertNull(map0.get("foo"));

        assertEquals(1, (int) map1.get("foo"));
        assertTrue(!map1.containsKey("bar"));

        assertEquals(1, (int) map2.get("foo"));
        assertEquals(2, (int) map2.get("bar"));

        assertTrue(!map3.containsKey("foo"));
        assertEquals(2, (int) map3.get("bar"));

        assertTrue(!map4.containsKey("foo"));
        assertEquals(3, (int) map4.get("bar"));

        assertEquals(4, (int) map5.get("foo"));
        assertEquals(3, (int) map5.get("bar"));

    }

    @Test
    public void testSize() {
        VMap<String, Integer> map = new VHashMap<String, Integer>(makePairs());
        assertEquals(3, map.size());
    }

    @Test
    public void testValueIterator() {
        // Using a Java Set just for testing.
        Integer[] values = new Integer[] { 1, 2, 3 };
        Set<Integer> valueSet = new HashSet<Integer>(Arrays.asList(values));

        VMap<String, Integer> map = new VHashMap<String, Integer>(makePairs());
        Iterator<Integer> iter = map.valueIterator();
        while (!valueSet.isEmpty()) {
            assertTrue(iter.hasNext());
            Integer value = iter.next();
            assertTrue(valueSet.contains(value));
            valueSet.remove(value);
        }
    }

    @Test
    public void testValueIterator2() {
        VMap<String, Integer> map0 = new VHashMap<String, Integer>();
        map0 = map0.put("red", 1);
        map0 = map0.put("orange", 2);
        VMap<String, Integer> map1 = map0.put("yellow", 3);
        VMap<String, Integer> map2 = map1.put("green", 4);
        VMap<String, Integer> map3 = map0.put("blue", 5);

        assertEquals(4, map2.size());
        assertEquals(3, map3.size());

        // Using a Java Set just for testing.
        Integer[] values = new Integer[] { 1, 2, 5 };
        Set<Integer> valueMap = new HashSet<Integer>(Arrays.asList(values));

        Iterator<Integer> iter = map3.valueIterator();
        while (iter.hasNext()) {
            Integer value = iter.next();
            assertTrue(valueMap.contains(value));
            valueMap.remove(value);
        }
        assertTrue(valueMap.isEmpty());
    }
}