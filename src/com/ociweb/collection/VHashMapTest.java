package com.ociweb.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.junit.*;

import static org.junit.Assert.*;

public class VHashMapTest {

    private void log(Object obj) { System.out.println(obj); }

    private Tuple[] makeTuples() {
        return new Tuple[] {
            new Tuple("foo", 1),
            new Tuple("bar", 2),
            new Tuple("baz", 3)
        };
    }

    @Test
    public void testClear() {
        VMap map0 = new VHashMap(makeTuples());
        assertEquals(3, map0.size());
        VMap map1 = map0.clear();
        assertEquals(0, map1.size());
    }

    @Test
    public void testContains() {
        VMap map = new VHashMap(makeTuples());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(map.containsKey("baz"));
        assertTrue(!map.containsKey("bad"));
    }

    @Test
    public void testDelete() {
        VMap map0 = new VHashMap();

        // It shouldn't be an error to try to delete a non-existent key.
        map0.delete("foo");

        VMap map1 = map0.put("foo", "bar");
        assertTrue(map1.getVersion() == map0.getVersion() + 1);

        VMap map2 = map1.delete("foo");
        assertTrue(map2.getVersion() == map1.getVersion() + 1);

        assertNull(map0.get("foo"));
        assertEquals("bar", map1.get("foo"));
        assertNull(map2.get("foo"));
    }

    @Test
    public void testKeyIterator() {
        // Using a Java Set just for testing.
        String[] keys = new String[] { "foo", "bar", "baz" };
        Set keySet = new HashSet(Arrays.asList(keys));

        VMap map = new VHashMap(makeTuples());
        Iterator iter = map.keyIterator();
        while (!keySet.isEmpty()) {
            assertTrue(iter.hasNext());
            Object key = iter.next();
            assertTrue(keySet.contains(key));
            keySet.remove(key);
        }
    }

    @Test
    public void testPerformance() {
        Set<String> words = Profile.getUniqueWords();
        log("words size = " + words.size()); // 1314
        String firstKey = "firstKey";

        //---------------------------------------------------------------------
        // Test standard Java map.
        //---------------------------------------------------------------------
        long startTime = System.currentTimeMillis();
        Map mMap = new HashMap();
        String prevWord = firstKey;
        for (String word : words) {
            mMap.put(prevWord, word);
            prevWord = word;
        }
        prevWord = firstKey;
        for (String word : words) {
            assertEquals(word, mMap.get(prevWord));
            prevWord = word;
        }
        long mMapElapsed = System.currentTimeMillis() - startTime;

        //---------------------------------------------------------------------
        // Test immutable map.
        //---------------------------------------------------------------------
        startTime = System.currentTimeMillis();
        VMap iMap = new VHashMap();
        prevWord = firstKey;
        for (String word : words) {
            iMap = iMap.put(prevWord, word);
            prevWord = word;
        }
        //iMap.dump();
        prevWord = firstKey;
        for (String word : words) {
            assertEquals(word, iMap.get(prevWord));
            prevWord = word;
        }
        long iMapElapsed = System.currentTimeMillis() - startTime;

        //---------------------------------------------------------------------
        // Compare their performance.
        //---------------------------------------------------------------------
        String msg =
            "immutable map performance (" + iMapElapsed + ") < " +
            "triple mutable map performance (" + mMapElapsed + ')';
        log(msg);
        assertTrue(msg, iMapElapsed < 3*mMapElapsed);
    }

    @Test
    public void testPutSingle() {
        VMap map0 = new VHashMap();
        VMap map1 = map0.put("foo", "bar");
        assertTrue(map1.getVersion() == map0.getVersion() + 1);

        assertNull(map0.get("foo"));
        assertEquals("bar", map1.get("foo"));
    }

    @Test
    public void testPutMultiple() {
        VMap map0 = new VHashMap();
        VMap map1 = map0.put(makeTuples());
        assertEquals(3, map1.size());
        assertEquals(1, map1.get("foo"));
        assertEquals(2, map1.get("bar"));
        assertEquals(3, map1.get("baz"));
    }

    @Test
    public void testPutRepeat() {
        VMap map0 = new VHashMap();
        VMap map1 = map0.put("foo", 1);
        VMap map2 = map1.put("bar", 2);
        VMap map3 = map2.delete("foo");
        VMap map4 = map3.put("bar", 3);
        VMap map5 = map4.put("foo", 4);

        assertTrue(!map0.containsKey("foo"));
        assertTrue(!map0.containsKey("bar"));

        assertEquals(1, map1.get("foo"));
        assertTrue(!map1.containsKey("bar"));

        assertEquals(1, map2.get("foo"));
        assertEquals(2, map2.get("bar"));

        assertTrue(!map3.containsKey("foo"));
        assertEquals(2, map3.get("bar"));

        assertTrue(!map4.containsKey("foo"));
        assertEquals(3, map4.get("bar"));

        assertEquals(4, map5.get("foo"));
        assertEquals(3, map5.get("bar"));

    }

    @Test
    public void testSize() {
        VMap map = new VHashMap(makeTuples());
        assertEquals(3, map.size());
    }

    @Test
    public void testValueIterator() {
        // Using a Java Set just for testing.
        Integer[] values = new Integer[] { 1, 2, 3 };
        Set valueSet = new HashSet(Arrays.asList(values));

        VMap map = new VHashMap(makeTuples());
        Iterator iter = map.valueIterator();
        while (!valueSet.isEmpty()) {
            assertTrue(iter.hasNext());
            Object value = iter.next();
            assertTrue(valueSet.contains(value));
            valueSet.remove(value);
        }
    }

}