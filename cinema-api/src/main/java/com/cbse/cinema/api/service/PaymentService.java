package com.cbse.cinema.api.service;

public interface PaymentService {
    // UC-20: For Credit/Debit Card (requires validation)
    boolean processCardPayment(int bookingId, String cardNum, String expiry, String cvv);
    
    // UC-20: For E-Wallet, Cash, or Bank Transfer
    boolean processOtherPayment(int bookingId, String method);

    // UC-21: Generate confirmation details
    void displayConfirmation(int bookingId);
}