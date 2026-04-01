package com.kpollman.team2;

/**
 * Inheritance for different queue management algorithms.
 */
public abstract class QueueAlgorithm {
    public abstract int calculateWaitTime(int queueLength, int activeStations);
}

class StandardWaitTimeAlgorithm extends QueueAlgorithm {
    @Override
    public int calculateWaitTime(int queueLength, int activeStations) {
        // Assume 5 minutes per voter per station on average
        if (activeStations <= 0) return 0;
        return (queueLength * 5) / activeStations;
    }
}

class PeakHourWaitTimeAlgorithm extends QueueAlgorithm {
    @Override
    public int calculateWaitTime(int queueLength, int activeStations) {
        // Assume 7 minutes per voter per station on average during peak hours
        if (activeStations <= 0) return 0;
        return (queueLength * 7) / activeStations;
    }
}
