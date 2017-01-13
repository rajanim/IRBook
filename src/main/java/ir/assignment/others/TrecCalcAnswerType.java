package ir.assignment.others;

import java.io.*;
import java.util.HashMap;

/**
 * Created by rajanishivarajmaski1 on 12/9/16.
 */
public class TrecCalcAnswerType {

    HashMap<String, String> tagQ = new HashMap<>();

    public static void main(String[] args) throws IOException {

        TrecCalcAnswerType trecCalcAnswerType = new TrecCalcAnswerType();
        trecCalcAnswerType.generateQrels();
        trecCalcAnswerType.generateEvalResults();

    }

    /**
     * This method is for generating output qrels
     * @throws IOException
     */
    private void generateEvalResults() throws IOException {

        // read test result file and extract answer-type and compare if its same as in gold truth file.
        //if it is same, score is 1 and otherwise zero. //write this to disc with [QryID] 0 [DocID] [Rank] [Score]

        InputStream stream = getClass().getClassLoader().getResourceAsStream("test_answer_type.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        File fOut = new File("output-qrels.txt");
        FileOutputStream fos = new FileOutputStream(fOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        StringBuffer buffer;
        int qryId = 1;
        int cnt = 0;
        //[QryID] 0 [DocID] [Rank] [Score] tfidf”
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            String tag = line.substring(0, line.indexOf(" ") + 1).trim();
            String key = line.substring(line.indexOf(" ")).trim();
            String value = tagQ.get(key);
            int score;
            if (value.equals(tag)) {
                score = 1;
                cnt++;
            } else {
                score = 0;
            }

            buffer = new StringBuffer()
                    .append(qryId).append(" ")
                    .append("0").append(" ")
                    .append(qryId).append(" ")
                    .append(score).append(" ")
                    .append(score).append(" ")
                    .append("binary");

            bw.write(buffer.toString());
            bw.newLine();
            qryId++;
        }

        // this count value is used to estimate correct number of types predicted given the test dataset
        System.out.println(cnt);

        bw.close();

    }

    /**
     * This method is to generate qrels for gold-truth results.
     *
     * @throws IOException
     */
    private void generateQrels() throws IOException {
        //read gold truth file and extract answer-type tag and create a tag and question map.
        //generate gold truth qrels “[QueryID] 0 [DocID] [Relevance]”

        tagQ = new HashMap<>();

        InputStream stream = getClass().getClassLoader().getResourceAsStream("train_answer_type.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        File fOut = new File("qrels.txt");
        FileOutputStream fos = new FileOutputStream(fOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        StringBuffer buffer = new StringBuffer();
        int qryId = 1;
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            tagQ.put(line.substring(line.indexOf(" ")).trim(), line.substring(0, line.indexOf(" ")).trim());
            buffer.append(qryId).append(" ").append("0").append(" ").append(qryId).append(" ").append("1");
            bw.write(buffer.toString());
            buffer = new StringBuffer();
            bw.newLine();
            qryId++;
        }
        bw.close();
    }
}
