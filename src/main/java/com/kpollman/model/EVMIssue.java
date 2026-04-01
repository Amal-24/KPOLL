package com.kpollman.model;

/**
 * EVM Issue subclass demonstrating inheritance.
 */
public class EVMIssue extends PollingIssue {
    public EVMIssue(int issueId, String description, int boothId, String urgencyLevel, String status) {
        super(issueId, "EVM", description, boothId, urgencyLevel, status);
    }

    @Override
    public String getResolutionGuide() {
        return "Call technical support. Check power connection. Replace EVM if necessary.";
    }
}
