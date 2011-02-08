package com.ociweb.collection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Profile {

    static List<String> wordList;

    public static void main(String[] args) {
        List<String> words = getWords();
        VSet iSet = load(words);
        check(words, iSet);
    }

    private static void check(List<String> words, VSet iSet) {
        for (String word : words) assert(iSet.contains(word));
    }

    private static String cleanWord(String word) {
        //String cleanWord = word.replaceAll("[^A-Z^a-z]", "");
        String cleanWord = word.replaceAll("\\W", "");
        //System.out.println("cleanWord: " + word + " -> " + cleanWord);
        return cleanWord;
    }

    static Set<String> getUniqueWords() {
        return new HashSet(getWords());
    }

    static List<String> getWords() {
        if (wordList != null) return wordList;

        wordList = new ArrayList<String>();

        //String text = "Alice was beginning to get very tired";
        //wordList.addAll(Arrays.asList(text.split(" ")));

        BufferedReader br = null;
        try {
            FileReader fr = new FileReader("data.txt");
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
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return wordList;
    }

    private static VSet load(List<String> words) {
        VSet iSet = new VHashSet();
        for (String word : words) iSet = iSet.add(word);
        return iSet;
    }
}