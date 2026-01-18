package com.cbse.cinema.api.model;

import java.io.Serializable;

public class Seat implements Serializable {
    private int seatNumber;
    private String type;
    private double price;
    private String status;

    // Default Constructor
    public Seat() {}

    // Getters and Setters
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}