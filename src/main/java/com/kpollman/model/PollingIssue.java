package com.kpollman.model;

import java.sql.Timestamp;

/**
 * Abstraction for Issue Resolution Workflow.
 */
public abstract class PollingIssue {
    private int issueId;
    private String category;
    private String description;
    private int boothId;
    private String urgencyLevel;
    private String status;
    private String assignedOfficial;
    private Timestamp reportedAt;
    private Timestamp resolvedAt;
    private String resolutionNotes;

    public PollingIssue(int issueId, String category, String description, int boothId, String urgencyLevel, String status) {
        this.issueId = issueId;
        this.category = category;
        this.description = description;
        this.boothId = boothId;
        this.urgencyLevel = urgencyLevel;
        this.status = status;
    }

    // Encapsulation: Getters and Setters
    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getBoothId() { return boothId; }
    public void setBoothId(int boothId) { this.boothId = boothId; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedOfficial() { return assignedOfficial; }
    public void setAssignedOfficial(String assignedOfficial) { this.assignedOfficial = assignedOfficial; }

    public Timestamp getReportedAt() { return reportedAt; }
    public void setReportedAt(Timestamp reportedAt) { this.reportedAt = reportedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    /**
     * Abstract method to handle resolution logic based on issue type.
     */
    public abstract String getResolutionGuide();
}
