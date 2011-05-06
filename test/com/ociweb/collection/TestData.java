package com.ociweb.collection;

import com.ociweb.io.IOUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides data for tests.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
class TestData {

    // The full text of four classic books is available.
    // Uncomment one of the four lines below.

    // "Alice in Wonderland" contains 26388 words of which 3153 are unique.
    //private static final String FILE_PATH = "data/AliceInWonderland.txt";

    // "Adventures of Tom Sawyer" contains 70040 words of which 8761 are unique.
    //private static final String FILE_PATH = "data/AdventuresOfTomSawyer.txt";

    // "Tale of Two Cities" contains 135820 words of which 11671 are unique.
    //private static final String FILE_PATH = "data/TaleOfTwoCities.txt";

    // "War and Peace" contains 562177 words of which 21843 are unique.
    private static final String FILE_PATH = "data/WarAndPeace.txt";

    static List<Pair<String, String>> pairs;

    static List<String> wordList;

    /**
     * Removes non-word characters from a word.
     * @param word the word
     * @return the cleaned word
     */
    private static String cleanWord(String word) {
        String cleanWord = word.replaceAll("\\W", "");
        //System.out.println("TestData.cleanWord: " + word + " -> " + cleanWord);
        return cleanWord;
    }

    static String getFirstKey() { return "firstKey"; }

    /**
     * Gets key/value pairs for testing hash tables.
     * @return a List of Pair objects
     */
    static List<Pair<String, String>> getPairs() {
        if (pairs != null) return pairs;

        // Put all the word pairs into a Java Map
        // so we can get the last value assigned to each key.
        Map<String, String> map = new HashMap<String, String>();
        String prevWord = getFirstKey();
        for (String word : getWords()) {
            map.put(prevWord, word);
            prevWord = word;
        }

        // Get the last value assigned to each key.
        pairs = new ArrayList<Pair<String, String>>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            pairs.add(new Pair<String, String>(
                entry.getKey(), entry.getValue()));
        }

        return pairs;
    }

    /**
     * Gets the unique words in FILE_PATH.
     * @return the words
     */
    static Set<String> getUniqueWords() {
        return new HashSet<String>(getWords());
    }

    /**
     * Gets all the words in FILE_PATH.
     * @return the words
     */
    static synchronized List<String> getWords() {
        if (wordList != null) return wordList;

        wordList = new ArrayList<String>();

        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(FILE_PATH);
            br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line == null) break;

                String[] arr = line.split(" ");
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = cleanWord(arr[i]);
                }
                for (String word : arr) {
                    if (!word.isEmpty()) wordList.add(word);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.close(br);
        }

        return wordList;
    }
}