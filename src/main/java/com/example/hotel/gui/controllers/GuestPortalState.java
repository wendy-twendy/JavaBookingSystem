package com.example.hotel.gui.controllers;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Room;

import java.time.LocalDate;

/**
 * Static state holder for the Guest Portal flow.
 * Enables passing data between portal screens.
 */
public class GuestPortalState {

    private static Room selectedRoom;
    private static LocalDate checkInDate;
    private static LocalDate checkOutDate;
    private static Booking createdBooking;

    private GuestPortalState() {
        // Static utility class
    }

    public static Room getSelectedRoom() {
        return selectedRoom;
    }

    public static void setSelectedRoom(Room room) {
        selectedRoom = room;
    }

    public static LocalDate getCheckInDate() {
        return checkInDate;
    }

    public static void setCheckInDate(LocalDate date) {
        checkInDate = date;
    }

    public static LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public static void setCheckOutDate(LocalDate date) {
        checkOutDate = date;
    }

    public static Booking getCreatedBooking() {
        return createdBooking;
    }

    public static void setCreatedBooking(Booking booking) {
        createdBooking = booking;
    }

    /**
     * Clears all state data.
     * Call when starting a new booking flow.
     */
    public static void clear() {
        selectedRoom = null;
        checkInDate = null;
        checkOutDate = null;
        createdBooking = null;
    }
}
