package com.example.myapplication.models;

import java.util.Date;

public class Ticket {
    private String id;
    private String userId;
    private String showtimeId;
    private String seatNumber;
    private Date bookingTime;
    private double totalPrice;
    private String status; // e.g., "BOOKED", "CANCELLED"

    public Ticket() {}

    public Ticket(String id, String userId, String showtimeId, String seatNumber, Date bookingTime, double totalPrice, String status) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.bookingTime = bookingTime;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public Date getBookingTime() { return bookingTime; }
    public void setBookingTime(Date bookingTime) { this.bookingTime = bookingTime; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}