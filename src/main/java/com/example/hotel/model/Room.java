package com.example.hotel.model;

import com.example.hotel.model.enums.RoomType;

public class Room extends AbstractEntity {
    private String roomNumber;
    private RoomType type;
    private double pricePerNight;
    private boolean available;
    private boolean refundable;

    // Default constructor for JSON deserialization
    public Room() {
    }

    public Room(String roomNumber, RoomType type, double pricePerNight,
                boolean available, boolean refundable) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.available = available;
        this.refundable = refundable;
    }

    @Override
    public String getId() {
        return roomNumber;
    }

    // Getters and Setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public void setRefundable(boolean refundable) {
        this.refundable = refundable;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", type=" + type +
                ", pricePerNight=" + pricePerNight +
                ", available=" + available +
                ", refundable=" + refundable +
                '}';
    }
}
