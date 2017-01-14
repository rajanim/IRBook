package ir.part1;

/**
 * Created by rajanishivarajmaski1 on 9/8/16.
 * Java object to maintain term - docId pairs
 */

public class Tuple_2 {

    private String term;

    private int docId;

    public Tuple_2(String term, int docId) {
        this.term = term;
        this.docId = docId;
    }

    public String getTerm() {
        return term;
    }

    public int getDocId() {
        return docId;
    }

}

