package ir.assignment.hw1;

import org.lemurproject.kstem.KrovetzStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by rajanishivarajmaski1 on 9/9/16.
 * Class to search conjunctive(AND) type queries with conjunctive algorithm.
 */
public class SearchConjunctiveQueries {

    static List<String> stopWords = Arrays.asList("the", "is", "at", "of", "on", "and", "a");
    //stemmer instance to analyse search terms
    static KrovetzStemmer stemmer = new KrovetzStemmer();
    //constants
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment1/";

    /*
    * Main method to initiate index file processing, search queries and write response to file
    * */
    public static void main(String[] args) throws IOException {

        //Reload indexes from file-system into memory as map data structure.
        File file = new File(directoryPath + "index.txt");
        Map<String, LinkedList<Integer>> map = reLoadIndexesFromFileToMap(file);

        //Search Queries:
        System.out.println("google AND asus: " + intersect(map.get(analyzeTerm("google")), map.get(analyzeTerm("asus"))));
        System.out.println();
        System.out.println("screen AND bad: " + intersect(map.get(analyzeTerm("screen")), map.get(analyzeTerm("bad"))));
        System.out.println();
        System.out.println("great AND tablet: " + intersect(map.get(analyzeTerm("great")), map.get(analyzeTerm("tablet"))));

    }

    /*
    * Method to read index file and load the vocabulary posting lists to memory
    * */
    private static Map<String, LinkedList<Integer>> reLoadIndexesFromFileToMap(File file) throws IOException {
        Map<String, LinkedList<Integer>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            String[] values = line.split("\\W+");
            String key = values[0];
            LinkedList<Integer> postLists = new LinkedList<>();
            for (int i = 1; i < values.length; i++) {
                postLists.add(Integer.parseInt(values[i]));
            }
            map.put(key, postLists);

        }
        return map;
    }

    /*
    * Analyze term using kstem
    * */
    private static String analyzeTerm(String term) {
        String ret = term.toLowerCase();
        if (stopWords.contains(ret) || ret.isEmpty())
            return "";
        return stemmer.stem(ret);
    }

    /*
    * Intersect algorithm implementation for conjunctive queries
    * */
    private static String intersect(LinkedList<Integer> p1, LinkedList<Integer> p2) {
        Set<Integer> answerSet = new HashSet<>();
        if (p1 != null && p2 != null) {
            System.out.println("p1 : " + p1.toString());
            System.out.println("p2 :" + p2.toString());
            while (!p1.isEmpty() && !p2.isEmpty()) {
                if (p1.peek() == p2.peek()) {
                    answerSet.add(p1.pop());
                } else if (p1.peek() < p2.peek()) {
                    p1.pop();
                } else {
                    p2.pop();
                }

            }
        }
        if (answerSet.toString().isEmpty())
            return "no match";
        return answerSet.toString();
    }


  /*  private static String positionIntersects(LinkedList<Pairs> p1, LinkedList<Pairs> p2) {
        Set<Integer> answerSet = new HashSet<>();
        if (p1 != null && p2 != null) {
            while (!p1.isEmpty() && !p2.isEmpty()) {
                if (p1.peek().getDocId() == p2.peek().getDocId()) {

                    answerSet.add(p1.pop());
                } else if (p1.peek() < p2.peek()) {
                    p1.pop();
                } else {
                    p2.pop();
                }

            }
        }
        if (answerSet.toString().isEmpty())
            return "no match";
        return answerSet.toString();
    }*/
}
