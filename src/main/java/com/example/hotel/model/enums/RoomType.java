package com.example.hotel.model.enums;

public enum RoomType {
    SINGLE("Single Room", 1),
    DOUBLE("Double Room", 2),
    SUITE("Suite", 4);

    private final String displayName;
    private final int maxOccupancy;

    RoomType(String displayName, int maxOccupancy) {
        this.displayName = displayName;
        this.maxOccupancy = maxOccupancy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
