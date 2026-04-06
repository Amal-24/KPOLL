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
    public abstract String generateReport(String constituencyName, double current, double historical);
}

class KeralaElectionAnalytics extends AnalyticsEngine {
    @Override
    public double calculateTurnoutPercentage(int totalVoted, int totalRegistered) {
        if (totalRegistered <= 0) return 0.0;
        return (totalVoted * 100.0) / totalRegistered;
    }

    @Override
    public String generateReport(String constituencyName, int turnout) {
        return "Turnout Summary for " + constituencyName + ":\n" +
               "Total Votes Counted: " + String.format("%,d", turnout);
    }

    @Override
    public String generateReport(String constituencyName, int male, int female, int third) {
        int total = male + female + third;
        return String.format("Detailed Gender Breakdown for %s:\n" +
                             "Male: %d (%.1f%%)\n" +
                             "Female: %d (%.1f%%)\n" +
                             "Third Gender: %d (%.1f%%)\n" +
                             "Total: %d", 
                             constituencyName, male, (male*100.0/total), female, (female*100.0/total), third, (third*100.0/total), total);
    }

    @Override
    public String generateReport(String constituencyName, double current, double historical) {
        double diff = current - historical;
        return String.format("Historical Trend for %s:\n" +
                             "Current Turnout: %.2f%%\n" +
                             "Previous Turnout: %.2f%%\n" +
                             "Swing: %+.2f%%", 
                             constituencyName, current, historical, diff);
    }
}
