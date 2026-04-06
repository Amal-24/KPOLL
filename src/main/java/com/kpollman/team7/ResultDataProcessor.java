package com.kpollman.team7;

/**
 * Abstraction for data processing in the live results dashboard.
 * Uses OOP concepts: Abstraction, Inheritance, and Polymorphism.
 */
public abstract class ResultDataProcessor {
    public abstract String processTrend(int leadingVotes, int runnerUpVotes);
    public abstract String analyzeMargin(int margin);
    
    // Polymorphic method for different types of visualizations
    public void displaySummary(String constituency, int leadingVotes, int runnerUpVotes) {
        String trend = processTrend(leadingVotes, runnerUpVotes);
        String marginStatus = analyzeMargin(leadingVotes - runnerUpVotes);
        System.out.println("Constituency: " + constituency + " | Trend: " + trend + " | Status: " + marginStatus);
    }
}

/**
 * Concrete implementation for Kerala Election Results.
 */
class KeralaLiveResultProcessor extends ResultDataProcessor {
    @Override
    public String processTrend(int leadingVotes, int runnerUpVotes) {
        int margin = leadingVotes - runnerUpVotes;
        if (margin > 10000) return "SAFE LEAD";
        if (margin > 5000) return "COMFORTABLE LEAD";
        if (margin > 0) return "CLOSE FIGHT";
        return "TIGHT CONTEST";
    }

    @Override
    public String analyzeMargin(int margin) {
        if (margin < 1000) return "Very Thin Margin";
        if (margin < 5000) return "Moderate Margin";
        return "Decisive Margin";
    }
}

/**
 * Another implementation for different context (e.g., historical analysis)
 */
class HistoricalResultProcessor extends ResultDataProcessor {
    @Override
    public String processTrend(int leadingVotes, int runnerUpVotes) {
        return "HISTORICAL DATA - NO LIVE TREND";
    }

    @Override
    public String analyzeMargin(int margin) {
        if (margin < 2000) return "HISTORICAL CLOSE CONTEST";
        return "HISTORICAL DECISIVE WIN";
    }
}
