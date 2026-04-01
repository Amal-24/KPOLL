package com.kpollman.model;

/**
 * Urban Booth subclass demonstrating inheritance.
 */
public class UrbanBooth extends Booth {
    public UrbanBooth(int id, String name, String constituency) {
        super(id, name, constituency, "Urban");
    }

    @Override
    public String getBoothAccessibilityDetails() {
        return "Wheelchair accessible, High-speed internet, Multiple entry points.";
    }
}
