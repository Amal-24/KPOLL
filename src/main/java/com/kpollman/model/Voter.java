package com.kpollman.model;

/**
 * Voter class demonstrating encapsulation.
 */
public class Voter {
    private String epicNo;
    private String name;
    private int age;
    private String photoPath;
    private int serialNo;
    private int boothId;
    private boolean votedStatus;

    public Voter(String epicNo, String name, int age, String photoPath, int serialNo, int boothId, boolean votedStatus) {
        this.epicNo = epicNo;
        this.name = name;
        this.age = age;
        this.photoPath = photoPath;
        this.serialNo = serialNo;
        this.boothId = boothId;
        this.votedStatus = votedStatus;
    }

    // Encapsulation: Getter and Setter methods
    public String getEpicNo() { return epicNo; }
    public void setEpicNo(String epicNo) { this.epicNo = epicNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public int getSerialNo() { return serialNo; }
    public void setSerialNo(int serialNo) { this.serialNo = serialNo; }

    public int getBoothId() { return boothId; }
    public void setBoothId(int boothId) { this.boothId = boothId; }

    public boolean isVotedStatus() { return votedStatus; }
    public void setVotedStatus(boolean votedStatus) { this.votedStatus = votedStatus; }
}
