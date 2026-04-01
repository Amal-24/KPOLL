package com.kpollman.model;

/**
 * Voter List Issue subclass demonstrating inheritance.
 */
public class VoterListIssue extends PollingIssue {
    public VoterListIssue(int issueId, String description, int boothId, String urgencyLevel, String status) {
        super(issueId, "VoterList", description, boothId, urgencyLevel, status);
    }

    @Override
    public String getResolutionGuide() {
        return "Verify with official electoral roll. Contact constituency registration officer.";
    }
}
