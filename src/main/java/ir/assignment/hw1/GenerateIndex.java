package ir.assignment.hw1;

import org.lemurproject.kstem.KrovetzStemmer;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rajanishivarajmaski1 on 9/8/16.
 *
 */
public class GenerateIndex {
    // constants
    private static final Logger LOGGER = Logger.getLogger(GenerateIndex.class.getName());
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment1/";
    List<String> stopWords = Arrays.asList("the", "is", "at", "of", "on", "and", "a");
    //list of key value terms
    List<Pairs> pairsList = new ArrayList<>();
    //stemmer instance
    KrovetzStemmer stemmer = new KrovetzStemmer();

    /**
     * Main method to read documents.txt and parse it to further create inverted indexes.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            //Logging Level for debugging purpose
            LOGGER.setLevel(Level.INFO);
            LOGGER.info("Reading input documents.txt file located at" + directoryPath);
            File file = new File(directoryPath + "documents.txt");
            LOGGER.info("Input file loaded");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            //Instance of current class to call methods.
            GenerateIndex generateIndex = new GenerateIndex();

            //append the multiple lines of a document to string buffer object
            StringBuffer buffer = null;
            int docId = 0;

            //Iterate through each line of documents.txt and split by DOC tag.
            for (String line = reader.readLine(); line != null; line = reader
                    .readLine()) {
                if (line.contains("DOC ")) {
                    buffer = new StringBuffer();
                    docId = Integer.parseInt(line.replace("<", "").replace(">", "").split(" ")[1]);
                    LOGGER.info("Processing doc with Doc ID: " + docId);
                } else if (line.contains("/DOC") && buffer != null) {
                    //end of doc tag. Pass the buffered text to method "indexDoc" in order to generate indexes for this doc.
                    generateIndex.indexDoc(buffer.toString(), docId);
                    buffer = null;
                } else {
                    if (buffer != null) {
                        buffer.append(" ");
                        buffer.append(line);
                    }
                }
            }
            //Write the indexes created for above documents to file system.
            generateIndex.sortWriteIndexesToFile();
            LOGGER.info("done");

        } catch (IOException io) {
            LOGGER.severe("File not found" + io.toString());
        }

    }

    /**
     * Sort by vocabulary and then posting lists that are stored as Pairs object
     *
     * @param pairsList
     */
    private static void order(List<Pairs> pairsList) {
        Collections.sort(pairsList, new Comparator() {
            public int compare(Object o1, Object o2) {

                String s1 = ((Pairs) o1).getTerm();
                String s2 = ((Pairs) o2).getTerm();
                int sComp = s1.compareTo(s2);

                if (sComp != 0) {
                    return sComp;
                } else {
                    Integer i1 = ((Pairs) o1).getDocId();
                    Integer i2 = ((Pairs) o2).getDocId();
                    return i1.compareTo(i2);
                }
            }
        });
    }

    /**
     * Iterate through each term in text, analyze and create a pair object with term vs docId
     *
     * @param docText
     * @param docId
     */
    private void indexDoc(String docText, int docId) {
        for (String _word : docText.split("\\W+")) {
            String word = _word.toLowerCase();
            if (stopWords.contains(word) || word.isEmpty())
                continue;
            pairsList.add(new Pairs(stemmer.stem(word), docId));
        }

    }

    /**
     * Sorts the key value pairs that are generated and creates map of term to documentID post-lists and writes to disk
     */
    private void sortWriteIndexesToFile() {
        //Sort indexes
        GenerateIndex.order(pairsList);
        //merge posting lists
        HashMap<String, Set<Integer>> postListMap = new HashMap<>();
        for (Pairs pair : pairsList) {
            if (postListMap.containsKey(pair.getTerm())) {
                Set<Integer> temp = postListMap.get(pair.getTerm());
                temp.add(pair.getDocId());
                postListMap.put(pair.getTerm(), temp);
            } else {
                Set<Integer> temp = new HashSet<Integer>();
                temp.add(pair.getDocId());
                postListMap.put(pair.getTerm(), temp);
            }
        }
        //write merged posting lists to disc
        writeMapToDisk(postListMap);

    }

    /**
     * Loop through the map and write elements to file
     *
     * @param mp
     */
    public void writeMapToDisk(Map mp) {
        try {
            File fOut = new File(directoryPath + "index.txt");
            FileOutputStream fos = new FileOutputStream(fOut);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            Iterator it = mp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                bw.write(pair.getKey() + " " + pair.getValue().toString());
                bw.newLine();
                it.remove();
            }
            bw.close();
        } catch (IOException io) {
            LOGGER.severe("output file not found or no write access to file" + io.toString());
        }

    }

}
