package ir.assignment.hw4.part2;

/**
 * Created by rajanishivarajmaski1 on 10/15/16.
 */
public class QueryTerms {

    public String getTerm() {
        return term;
    }

    public QueryTerms(String term, double score) {
        this.term = term;
        this.score = score;
    }

    String term;

    public double getScore() {
        return score;
    }

    double score;

}
