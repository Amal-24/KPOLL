package com.kpollman.model;

/**
 * Encapsulation for Turnout Data Model.
 */
public class TurnoutData {
    private int boothId;
    private int hour;
    private int maleVotes;
    private int femaleVotes;
    private int thirdGenderVotes;

    public TurnoutData(int boothId, int hour, int maleVotes, int femaleVotes, int thirdGenderVotes) {
        this.boothId = boothId;
        this.hour = hour;
        this.maleVotes = maleVotes;
        this.femaleVotes = femaleVotes;
        this.thirdGenderVotes = thirdGenderVotes;
    }

    // Encapsulation: Getters and Setters
    public int getBoothId() { return boothId; }
    public void setBoothId(int boothId) { this.boothId = boothId; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMaleVotes() { return maleVotes; }
    public void setMaleVotes(int maleVotes) { this.maleVotes = maleVotes; }

    public int getFemaleVotes() { return femaleVotes; }
    public void setFemaleVotes(int femaleVotes) { this.femaleVotes = femaleVotes; }

    public int getThirdGenderVotes() { return thirdGenderVotes; }
    public void setThirdGenderVotes(int thirdGenderVotes) { this.thirdGenderVotes = thirdGenderVotes; }

    public int getTotalVotes() {
        return maleVotes + femaleVotes + thirdGenderVotes;
    }
}
