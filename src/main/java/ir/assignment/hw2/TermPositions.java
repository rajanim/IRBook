package ir.assignment.hw2;

import java.util.LinkedList;

/**
 * Created by rajanishivarajmaski1 on 9/8/16.
 * Java object to maintain term - docId and term positions
 */


class TermPositions {
    private int docId;
    private LinkedList<Integer> positionList;

    public TermPositions(int docId, LinkedList<Integer> positionList) {
        this.docId = docId;
        this.positionList = positionList;
    }

    public LinkedList<Integer> getPositionList() {
        return positionList;
    }

    public int getDocId() {
        return docId;
    }

    public String toString() {
        return docId + ": " + positionList.toString();
    }


}