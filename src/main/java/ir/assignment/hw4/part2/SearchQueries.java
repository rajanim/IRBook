package ir.assignment.hw4.part2;

import org.lemurproject.kstem.KrovetzStemmer;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by rajanishivarajmaski.
 * Class to search for documents based on proximity and relevancy, extended for query expansion.
 */
public class SearchQueries {

    //index file path
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment_4/";
    //objects
    public Map<String, LinkedList<TermPositions>> indexMap = new HashMap<>();
    public List<DocCollector> resultsCollector = new ArrayList<>();
    KrovetzStemmer stemmer = new KrovetzStemmer();
    List<String> stopWords = Arrays.asList("the", "is", "at", "of", "on", "and", "a", "it", "to", "this", "i", "my", "for", "with");
    //Total documents hardcoded, required for calculating IDF.
    int N = 10;
    //x parameter for query expansion.
    int x = 5;
    int qryId = 2;

    /**
     * Scan user input and invoke methods to load indexes -> parse user search query -> search ->
     * Apply query expansion technique -> re-search
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        //Reload indexes from file-system into memory as map data structure.
        System.out.println("Loading indexes in memory");
        File file = new File(directoryPath + "index.txt");
        SearchQueries searchQueries = new SearchQueries();
        searchQueries.reLoadIndexesFromFileToMemory(file);

        //Get query search string
        System.out.println("Enter search query string");
        Scanner scanner = new Scanner(System.in);
        String searchString = scanner.nextLine();

        // search, if matches, do next step otherwise exit.
        System.out.println("Searching for matching documents...");
        searchQueries.parseQueryStringAndSearch(searchString);

        //If matches exist, re phrase query with query expansion techniques (PRF & Synonyms approach)
        //  Re execute search and print new results to file.
        if (!searchQueries.resultsCollector.isEmpty())
            searchQueries.researchAndPrintResultsToFile(searchString);
        else {

            // Implementing query expansion by synonyms concept
            Synonyms.loadSynonymsToMemory();
             String synonyms = Synonyms.lookUpSynonyms(searchString);
            if(!synonyms.isEmpty()) {
                searchQueries.parseQueryStringAndSearch(synonyms);
                searchQueries.printResultsCollectorToFile();
            }
                else
            System.out.println("no matches");
        }


    }


    /**
     * Read index file line-line -> parse posting list -> map to java objects.
     *
     * @param file
     * @throws IOException
     */
    private void reLoadIndexesFromFileToMemory(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            String[] termIndex = line.split(" ");
            String key = termIndex[0];
            LinkedList<TermPositions> value = new LinkedList<>();
            String postingList = termIndex[1];
            // split the posting lists  1:99;2:14;8:19,82
            String[] subPostings = postingList.split(";");
            double df = subPostings.length;
            for (String posting : subPostings) {
                String[] docPosting = posting.split(":");
                double tf = docPosting.length;
                int docId = Integer.parseInt(docPosting[0]);
                String[] positions = docPosting[1].split(",");
                LinkedList<Integer> positionsList = new LinkedList<>();
                for (String position : positions) {
                    positionsList.add(Integer.parseInt(position));
                }
                TermPositions termPositions = new TermPositions(docId, positionsList);
                double score = ((double) 1 + Math.log10(tf) *
                        (Math.log10((double) (N) / df)));
                termPositions.setScore(score);
                value.add(termPositions);
            }
            indexMap.put(key, value);

        }

    }


    /**
     * To parse user query that may contain proximity type syntax or terms separated by spaces.
     * Invoke search.
     *
     * @param searchString
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
                System.out.println(query + " and k: " + k);
                positionIntersects(indexMap.get(analyzeTerm(terms[0])), indexMap.get(analyzeTerm(terms[1])), k);
            } else {
                queue = new LinkedList<>();
                while (i < chars.length && !Character.isDigit(chars[i])) {
                    queue.add(chars[i]);
                    i++;
                }
                i--;
                String query = queryToString(queue);
                System.out.println(query.trim());
                String[] terms = query.trim().split(" ");
                termQuerySearch(terms);
            }

        }

    }


    /**
     * Convert characters queue to String.
     *
     * @param queue
     * @return query terms as string
     */
    private String queryToString(Queue<Character> queue) {
        Iterator<Character> it = queue.iterator();
        StringBuffer bufferSt = new StringBuffer();
        while (it.hasNext()) {
            bufferSt.append(queue.poll());
        }
        return bufferSt.toString().replace(")", "").replace("(", "").trim();
    }

    /**
     * Search for individual terms
     *
     * @param terms
     */
    private void termQuerySearch(String[] terms) {
        for (String term : terms) {
            LinkedList<TermPositions> postingLists = indexMap.get(analyzeTerm(term.trim()));
            if (postingLists != null) {
                for (TermPositions matches : postingLists) {
                    double score = (double) 1 + Math.log10((double) matches.getPositionList().size()) *
                            (Math.log10((double) (N) / (double) postingLists.size()));
                    resultsCollector.add(new DocCollector(matches.getDocId(), score));
                }
            }
        }
    }

    /**
     * Intersects two-terms posting list with proximity paramter k.
     *
     * @param p1 posting list of term 1
     * @param p2 posting list of term 2
     * @param k  proximity parameter
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
                                double score = (1 + Math.log10(tf1) * Math.log10(N / df1)) + (1 + Math.log10(tf2) * Math.log10(N / df2));
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


    /**
     * Invokes methods to obtain new query from query expansion technique.
     * Invokes the execution of query parsing and search method.
     *
     * @param userSearchQuery
     */
    private void researchAndPrintResultsToFile(String userSearchQuery) throws IOException {
        int docId = getFirstMatchDocId();
        List<QueryTerms> queryTermsList = getQueryExpansionTerms(docId);
        StringBuffer buffer = new StringBuffer();
        Set<String> set = new HashSet<>();
        set.add(userSearchQuery);
      int i=0;
      while(set.size()!=x){
            set.add(queryTermsList.get(i).getTerm());
          i++;
        }
        resultsCollector = new ArrayList<>();
        for(String string : set)
            buffer.append(string).append(" ");
        System.out.println("Expanded query string: " + buffer.toString());
        parseQueryStringAndSearch(buffer.toString());
        printResultsCollectorToFile();

    }

    /**
     * Prints final matching results in [QryID] 0 [DocID] [Rank] [Score] tfidf format
     */
    private void printResultsCollectorToFile() {
        try {
            File fOut = new File(directoryPath + "output.txt");
            FileOutputStream fos = new FileOutputStream(fOut);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            HashMap<Integer, Double> resultsMap = new HashMap<>();
            for (DocCollector collector : resultsCollector) {
                if (resultsMap.containsKey(collector.getDocId())) {
                    double score = resultsMap.get(collector.getDocId()) + collector.getTermScore();
                    resultsMap.put(collector.getDocId(), score);
                } else {
                    resultsMap.put(collector.getDocId(), collector.getTermScore());
                }
            }
            Iterator it = resultsMap.entrySet().iterator();
            DecimalFormat df = new DecimalFormat(".####");
            int rank = 0;
            while (it.hasNext()) {
                Map.Entry doc = (Map.Entry)it.next();
                rank++;
                StringBuffer buffer = new StringBuffer()
                        .append(qryId).append(" ")
                        .append("0").append(" ")
                        .append(doc.getKey()).append(" ")
                        .append(rank).append(" ")
                        .append(df.format(doc.getValue())).append(" ")
                        .append("tfidf");

                bw.write(buffer.toString());
                bw.newLine();
                it.remove();
            }
            bw.close();
        } catch (IOException io) {
            System.err.print("output file not found or no write access to file" + io.toString());
        }


    }

    /**
     * New query expansion technique requires the docId of first match document of original query.
     * This method sorts search results collector, sort by score, and returns first doc ID
     *
     * @return docId
     */
    private int getFirstMatchDocId() {
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
        return list.get(0).getKey();
    }

    /**
     * New query expansion technique requires to fetch document content of the first matching document.
     * This method invokes read from file for that document object content.
     *
     * @param docId
     * @return for given docId, terms list of document-content with tf-idf score applied to each term.
     */
    private List<QueryTerms> getQueryExpansionTerms(int docId) throws IOException {
        String[] terms = getDocumentTermsFromFile(docId);
        List<QueryTerms> queryTermsList = new ArrayList<>();
        for (String term : terms) {
            LinkedList<TermPositions> postingLists = indexMap.get(analyzeTerm(term));
            if (postingLists != null) {
                for (TermPositions matches : postingLists) {
                    if (matches.getDocId() == docId) {
                        queryTermsList.add(new QueryTerms(term, matches.getScore()));
                    }
                }

            }
            order(queryTermsList);
        }
        return queryTermsList;
    }

    /**
     * Read document file to get document text of given docId
     *
     * @param docId
     * @return term list of content, split by space and special characters.
     * @throws IOException
     */
    private String[] getDocumentTermsFromFile(int docId) throws IOException {
        File file = new File(directoryPath + "documents.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer buffer = new StringBuffer();
        int curDocId;
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            if (line.contains("DOC ")) {
                curDocId = Integer.parseInt(line.replace("<", "").replace(">", "").split(" ")[1]);
                if (curDocId == docId) {
                    String curLine = reader.readLine().trim();
                    while (!curLine.contains("/DOC")) {
                        buffer.append(curLine);
                        curLine = reader.readLine();
                    }

                    break;
                }
            }
        }
        return buffer.toString().split("\\W+");
    }

    /**
     * Sort method to sort query terms list by tf-idf score.
     *
     * @param queryTermsList
     */
    private static void order(List<QueryTerms> queryTermsList) {
        Collections.sort(queryTermsList, new Comparator() {
            public int compare(Object o1, Object o2) {
                Double i1 = ((QueryTerms) o1).getScore();
                Double i2 = ((QueryTerms) o2).getScore();
                return i1.compareTo(i2);
            }
        });
    }

    /**
     * Analyzer that applies stopword and stemming filters.
     *
     * @param term
     * @return analysed Term
     */
    private String analyzeTerm(String term) {
        String ret = term.toLowerCase();
        if (stopWords.contains(ret) || ret.isEmpty() || ret.length()==1)
            return "";
        return stemmer.stem(ret);
    }

}
