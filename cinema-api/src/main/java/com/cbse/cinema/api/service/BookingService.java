package com.cbse.cinema.api.service;

import java.util.List;

public interface BookingService {
    double calculateFinalTotal(List<Integer> seats, double discount);
    
    double calculateSubtotal(List<Integer> seats);
    
    int createBooking(String userId, String sessionId, List<Integer> seats, double total, String promoCode);

    double getPromoDiscount(String promoCode, double subtotal);

    List<Integer> getSeatsByBooking(int bookingId);

    boolean updateBookingTotal(int bookingId, double newTotal, String promoCode);

    String cancelBooking(int bookingId);

    void listUserBookings(String userId);

    void getBookingDetails(int bookingId);
}