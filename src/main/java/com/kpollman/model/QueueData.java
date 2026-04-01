package com.kpollman.model;

import java.sql.Timestamp;

/**
 * Encapsulation for Queue Data.
 */
public class QueueData {
    private int boothId;
    private int currentQueueLength;
    private int avgWaitTimeMins;
    private int activeStations;
    private Timestamp lastUpdated;

    public QueueData(int boothId, int currentQueueLength, int avgWaitTimeMins, int activeStations, Timestamp lastUpdated) {
        this.boothId = boothId;
        this.currentQueueLength = currentQueueLength;
        this.avgWaitTimeMins = avgWaitTimeMins;
        this.activeStations = activeStations;
        this.lastUpdated = lastUpdated;
    }

    // Encapsulation: Getters and Setters
    public int getBoothId() { return boothId; }
    public void setBoothId(int boothId) { this.boothId = boothId; }

    public int getCurrentQueueLength() { return currentQueueLength; }
    public void setCurrentQueueLength(int currentQueueLength) { this.currentQueueLength = currentQueueLength; }

    public int getAvgWaitTimeMins() { return avgWaitTimeMins; }
    public void setAvgWaitTimeMins(int avgWaitTimeMins) { this.avgWaitTimeMins = avgWaitTimeMins; }

    public int getActiveStations() { return activeStations; }
    public void setActiveStations(int activeStations) { this.activeStations = activeStations; }

    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}
