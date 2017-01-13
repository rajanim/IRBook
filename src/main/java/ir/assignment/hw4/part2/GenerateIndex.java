package ir.assignment.hw4.part2;

import org.lemurproject.kstem.KrovetzStemmer;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rajanishivarajmaski1 on 9/8/16.
 * Class to generate inverted index
 */
public class GenerateIndex {

    // constants
    private static final Logger LOGGER = Logger.getLogger(GenerateIndex.class.getName());
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment_4/";
    List<String> stopWords = Arrays.asList("the", "is", "at", "of", "on", "and", "a");

    //stemmer instance
    KrovetzStemmer stemmer = new KrovetzStemmer();

    //indexMap
    Map<String, StringBuffer> indexMap = new HashMap<>();


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
            generateIndex.writeMapToDisk();
            LOGGER.info("done");

        } catch (IOException io) {
            LOGGER.severe("File not found" + io.toString());
        }

    }


    /**
     * Iterate through each term in text, analyze and
     * create a map of term to docId coupled with term positions
     *
     * @param docText
     * @param docId
     */
    private void indexDoc(String docText, int docId) {
        HashMap<String, StringBuffer> tempMap = new HashMap<>();
        int position = 0;
        StringBuffer buffer;
        for (String _word : docText.split("\\W+")) {
            String word = _word.toLowerCase();
            word = stemmer.stem(word);
            if (stopWords.contains(word) || word.isEmpty()) {
                position++;
                continue;
            }
            if (tempMap.containsKey(word)) {
                buffer = tempMap.get(word);
                buffer.append(",").append(position);
                tempMap.put(word, buffer);
                position++;
            } else {
                buffer = new StringBuffer();
                buffer.append(docId).append(":");
                buffer.append(position);
                tempMap.put(word, buffer);
                position++;
            }
        }
        updateToIndexMap(tempMap);

    }

    /**
     * Iterate through above single document's terms map and update posting lists to full term index map.
     *
     * @param tempMap
     */
    private void updateToIndexMap(HashMap<String, StringBuffer> tempMap) {
        StringBuffer buffer;
        Iterator it = tempMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry eachMap = (Map.Entry) it.next();
            if (indexMap.containsKey(eachMap.getKey())) {
                buffer = indexMap.get(eachMap.getKey());
                buffer.append(";").append(eachMap.getValue());
            } else {
                indexMap.put(eachMap.getKey().toString(), (StringBuffer) eachMap.getValue());
            }
            it.remove();
        }

    }


    /**
     * Loop through the full index map and write elements to file
     */
    public void writeMapToDisk() {
        try {
            File fOut = new File(directoryPath + "index.txt");
            FileOutputStream fos = new FileOutputStream(fOut);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            Iterator it = indexMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry eachMap = (Map.Entry) it.next();
                bw.write(eachMap.getKey() + " " + eachMap.getValue());
                bw.newLine();
                it.remove();
            }
            bw.close();
        } catch (IOException io) {
            LOGGER.severe("output file not found or no write access to file" + io.toString());
        }

    }


}
