package com.kpollman.team7;

/**
 * Abstraction for data processing in the live results dashboard.
 */
public abstract class ResultDataProcessor {
    public abstract String processTrend(int leadingVotes, int runnerUpVotes);
}

class KeralaLiveResultProcessor extends ResultDataProcessor {
    @Override
    public String processTrend(int leadingVotes, int runnerUpVotes) {
        int margin = leadingVotes - runnerUpVotes;
        if (margin > 10000) return "SAFE LEAD (" + margin + ")";
        if (margin > 5000) return "COMFORTABLE LEAD (" + margin + ")";
        if (margin > 0) return "CLOSE FIGHT (" + margin + ")";
        return "TIGHT CONTEST";
    }
}
