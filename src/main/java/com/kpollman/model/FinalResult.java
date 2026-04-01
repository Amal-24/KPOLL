package com.kpollman.model;

import java.sql.Timestamp;

/**
 * Encapsulation for final results data.
 */
public class FinalResult {
    private int candidateId;
    private int constituencyId;
    private int totalVotes;
    private double voteShare;
    private boolean winner;
    private Timestamp certifiedAt;

    public FinalResult(int candidateId, int constituencyId, int totalVotes, double voteShare, boolean winner, Timestamp certifiedAt) {
        this.candidateId = candidateId;
        this.constituencyId = constituencyId;
        this.totalVotes = totalVotes;
        this.voteShare = voteShare;
        this.winner = winner;
        this.certifiedAt = certifiedAt;
    }

    public int getCandidateId() { return candidateId; }
    public int getConstituencyId() { return constituencyId; }
    public int getTotalVotes() { return totalVotes; }
    public double getVoteShare() { return voteShare; }
    public boolean isWinner() { return winner; }
    public Timestamp getCertifiedAt() { return certifiedAt; }
}
