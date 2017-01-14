package ir.part2;

import ir.assignment.hw2.DocCollector;
import org.lemurproject.kstem.KrovetzStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by rajanishivarajmaski.
 * Class to search for documents based on proximity and relevancy.
 */
public class SearchQueries {

    static List<String> stopWords = Arrays.asList("the", "is", "at", "of", "on", "and", "a");
    //Stemmer instance to analyse search terms
    static KrovetzStemmer stemmer = new KrovetzStemmer();
    //constants
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment2/";
    public Map<String, LinkedList<TermPositions>> indexMap = new HashMap<>();
    public List<DocCollector> resultsCollector = new ArrayList<>();
    //Total documents hardcoded, that which is required for calculating IDF.
    int N = 10;

    /*
   * Main method to initiate index file processing, search queries and write response to file
   * */
    public static void main(String[] args) throws IOException {

        //Reload indexes from file-system into memory as map data structure.
        System.out.println("Loading indexes in memory");
        File file = new File(directoryPath + "index.txt");
        SearchQueries searchQueries = new SearchQueries();
        searchQueries.reLoadIndexesFromFileToIndexMap(file);

        System.out.println("Enter search query string");
        Scanner scanner = new Scanner(System.in);
        String searchString = scanner.nextLine();

        System.out.println("Searching for matching documents...");
        searchQueries.parseQueryStringAndSearch(searchString);

        searchQueries.printingSearchResults();


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
    Method reads index.txt reloads each term's posting lists with term positions into list of TermPositions object
     */
    private void reLoadIndexesFromFileToIndexMap(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            String[] termIndex = line.split(" ");
            String key = termIndex[0];
            LinkedList<TermPositions> value = new LinkedList<>();
            String postingList = termIndex[1];
            // split the posting lists 1:1,2,4;2:4
            String[] subPostings = postingList.split(";");
            for (String posting : subPostings) {
                String[] docPosting = posting.split(":");
                int docId = Integer.parseInt(docPosting[0]);
                String[] positions = docPosting[1].split(",");
                LinkedList<Integer> positionsList = new LinkedList<>();
                for (String position : positions) {
                    positionsList.add(Integer.parseInt(position));
                }
                TermPositions termPositions = new TermPositions(docId, positionsList);
                value.add(termPositions);
            }
            indexMap.put(key, value);

        }

    }

    /*
    *This method parses query string, checks for digit to parse 'k' for proximity type queries.
    * otherwise treats each term individually and searches for each term.
  */
    public void parseQueryStringAndSearch(String searchString) {
        char[] chars = searchString.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Queue<Character> queue;
            int k;
            if (Character.isDigit(chars[i])) {
                k = Integer.parseInt(String.valueOf(chars[i]));
                i++;
                queue = new LinkedList<>();
                while (i < chars.length && chars[i] != ')') {
                    queue.add(chars[i]);
                    i++;
                }
                i++;
                String query = queryToString(queue);
                String[] terms = query.split(" ");
                //System.out.println(query + " and k: " + k);
                positionIntersects(indexMap.get(analyzeTerm(terms[0])), indexMap.get(analyzeTerm(terms[1])), k);
            } else {
                queue = new LinkedList<>();
                while (i < chars.length && !Character.isDigit(chars[i])) {
                    queue.add(chars[i]);
                    i++;
                }
                i--;
                String query = queryToString(queue);
                String[] terms = query.trim().split(" ");
                termQuerySearch(terms);
            }

        }

    }

    /*
    * Helper method to convert search string character array to string.
  */
    private String queryToString(Queue<Character> queue) {
        Iterator<Character> it = queue.iterator();
        StringBuffer bufferSt = new StringBuffer();
        while (it.hasNext()) {
            bufferSt.append(queue.poll());
        }
        return bufferSt.toString().replace(")", "").replace("(", "").trim();
    }

    /*
 * Search method for individual terms
  */
    private void termQuerySearch(String[] terms) {
        for (String term : terms) {
            LinkedList<TermPositions> postingLists = indexMap.get(analyzeTerm(term));
            if (postingLists != null) {
                for (TermPositions matches : postingLists) {
                    double score = (double) 1 + Math.log((double) matches.getPositionList().size()) *
                            Math.log((double) N / (double) postingLists.size());
                    resultsCollector.add(new DocCollector(matches.getDocId(), score));
                }
            }
        }
    }

    /*
     *This method is for positional intersections search
      */
    private void positionIntersects(LinkedList<TermPositions> p1, LinkedList<TermPositions> p2, int k) {
        if (p1 != null && p2 != null) {
            int df1 = p1.size();
            int df2 = p2.size();
            while (!p1.isEmpty() && !p2.isEmpty()) {
                int tf1 = p1.peek().getPositionList().size();
                int tf2 = p1.peek().getPositionList().size();
                if (p1.peek().getDocId() == p2.peek().getDocId()) {
                    LinkedList<Integer> pp1 = p1.peek().getPositionList();
                    while (!pp1.isEmpty()) {
                        for (Integer integer : p2.peek().getPositionList()) {
                            if ((pp1.peek() - integer) < 0 && Math.abs(pp1.peek() - integer) <= (k + 1)) {
                                double score = (1 + Math.log(tf1) * Math.log(N / df1)) + (1 + Math.log(tf2) * Math.log(N / df2));
                                resultsCollector.add(new DocCollector(p1.peek().getDocId(), score));
                                break;
                            }
                        }
                        pp1.pop();
                    }
                } else if (p1.peek().getDocId() < p2.peek().getDocId())
                    p1.pop();
                else
                    p2.pop();

                p1.pop();
                p2.pop();

            }

        }


    }


    /*
 *To loop through matching documents list and sort by score and print results to console
  */
    private void printingSearchResults() {
        HashMap<Integer, Double> resultsMap = new HashMap<>();
        for (DocCollector collector : resultsCollector) {
            if (resultsMap.containsKey(collector.getDocId())) {
                double score = resultsMap.get(collector.getDocId()) + collector.getTermScore();
                resultsMap.put(collector.getDocId(), score);
            } else {
                resultsMap.put(collector.getDocId(), collector.getTermScore());
            }
        }
        List<Map.Entry<Integer, Double>> list = new LinkedList<>(resultsMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        if (list.isEmpty())
            System.out.println("no match");
        else {
              DecimalFormat df = new DecimalFormat(".##");
            for (Map.Entry<Integer, Double> each : list)
                System.out.println("docId: " + each.getKey() + " score: " + df.format(each.getValue()));
        }

    }


}
