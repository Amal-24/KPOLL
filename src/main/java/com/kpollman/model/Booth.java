package com.kpollman.model;

/**
 * Base class for a Polling Booth.
 * Demonstrates Encapsulation and Abstraction (if made abstract).
 */
public abstract class Booth {
    private int id;
    private String name;
    private String constituency;
    private String type;

    public Booth(int id, String name, String constituency, String type) {
        this.id = id;
        this.name = name;
        this.constituency = constituency;
        this.type = type;
    }

    // Encapsulation: Getter and Setter methods
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getConstituency() { return constituency; }
    public void setConstituency(String constituency) { this.constituency = constituency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /**
     * Abstract method to be implemented by subclasses to show booth-specific logic.
     */
    public abstract String getBoothAccessibilityDetails();
}
