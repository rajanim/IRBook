package ir.part2;

/**
 * Created by rajanishivarajmaski1 on 9/27/16.
 * <p>
 * Matching documents collector pojo with score
 */


public class Tuple_2 {

    int docId;
    double termScore;

    public Tuple_2(int docId, double termScore) {
        this.docId = docId;
        this.termScore = termScore;
    }

    public int getDocId() {
        return docId;
    }

    public double getTermScore() {
        return termScore;
    }


}
