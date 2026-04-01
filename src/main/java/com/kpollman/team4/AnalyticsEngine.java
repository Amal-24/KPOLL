package com.kpollman.team4;

/**
 * Inheritance for different analytics engines.
 */
public abstract class AnalyticsEngine {
    public abstract double calculateTurnoutPercentage(int totalVoted, int totalRegistered);
    
    /**
     * Method Overloading for generating different report summaries.
     */
    public abstract String generateReport(String constituencyName, int turnout);
    public abstract String generateReport(String constituencyName, int male, int female, int third);
}

class KeralaElectionAnalytics extends AnalyticsEngine {
    @Override
    public double calculateTurnoutPercentage(int totalVoted, int totalRegistered) {
        if (totalRegistered <= 0) return 0.0;
        return (totalVoted * 100.0) / totalRegistered;
    }

    @Override
    public String generateReport(String constituencyName, int turnout) {
        return "Constituency: " + constituencyName + " | Total Turnout: " + turnout;
    }

    @Override
    public String generateReport(String constituencyName, int male, int female, int third) {
        return String.format("Constituency: %s | M: %d, F: %d, TG: %d", constituencyName, male, female, third);
    }
}
