package com.kpollman.model;

/**
 * Encapsulation for Round-wise result storage.
 */
public class RoundResult {
    private int resultId;
    private int roundNo;
    private int tableId;
    private int candidateId;
    private int votesCounted;
    private boolean verified;

    public RoundResult(int resultId, int roundNo, int tableId, int candidateId, int votesCounted, boolean verified) {
        this.resultId = resultId;
        this.roundNo = roundNo;
        this.tableId = tableId;
        this.candidateId = candidateId;
        this.votesCounted = votesCounted;
        this.verified = verified;
    }

    public int getResultId() { return resultId; }
    public int getRoundNo() { return roundNo; }
    public int getTableId() { return tableId; }
    public int getCandidateId() { return candidateId; }
    public int getVotesCounted() { return votesCounted; }
    public boolean isVerified() { return verified; }
}
