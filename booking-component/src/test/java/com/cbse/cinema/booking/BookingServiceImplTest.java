package com.cbse.cinema.booking;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cbse.cinema.api.service.DatabaseService;

public class BookingServiceImplTest {

    @Mock private DatabaseService dbService;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbService.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // Required for createArrayOf logic
        when(connection.createArrayOf(anyString(), any())).thenReturn(mock(Array.class));
    }

    // --- UC-18: Calculate Price ---
    @Test
    void testCalculateSubtotal_MixedSeats() {
        // Seats 27 (Premium), 60 (VIP), 5 (Early Bird), 10 (Standard)
        List<Integer> seats = Arrays.asList(27, 60, 5, 10);
        // 25.0 + 30.0 + 15.0 + 10.0 = 80.0
        double result = bookingService.calculateSubtotal(seats);
        assertEquals(80.0, result, 0.01);
    }

    @Test
    void testCalculateFinalTotal_WithDiscount() {
        List<Integer> seats = Arrays.asList(10, 11); // 10.0 + 10.0 = 20.0
        // Subtotal: 20, Fee (10%): 2, Discount: 5
        // Tax (10% of (20+2-5)): 1.7
        // Total: 20 + 2 + 1.7 - 5 = 18.7
        double total = bookingService.calculateFinalTotal(seats, 5.0);
        assertEquals(18.7, total, 0.01);
    }

    // --- UC-17: Create Booking ---
    @Test
    void testCreateBooking_Success() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true); 
        when(resultSet.getInt(1)).thenReturn(2, 500); // 1st call for check (2 seats), 2nd for return ID

        int bookingId = bookingService.createBooking("user1", "sess1", Arrays.asList(1, 2), 50.0, "PROMO");

        assertEquals(500, bookingId);
        verify(connection).commit();
    }

    @Test
    void testCreateBooking_SeatLockConflict() throws Exception {
        // UC-17 Exception Flow: Seat no longer available/locked by user
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // User wants 2 seats, but only 1 is locked

        int result = bookingService.createBooking("user1", "sess1", Arrays.asList(1, 2), 50.0, null);

        assertEquals(-1, result);
    }

    // --- UC-19: Apply Promo Code ---
    @Test
    void testGetPromoDiscount_Valid() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("discount_type")).thenReturn("PERCENT");
        when(resultSet.getDouble("discount_value")).thenReturn(20.0);
        when(resultSet.getDouble("min_purchase")).thenReturn(30.0);

        double discount = bookingService.getPromoDiscount("SAVE20", 50.0);
        assertEquals(10.0, discount); // 20% of 50
    }

    @Test
    void testGetPromoDiscount_MinPurchaseNotMet() throws Exception {
        // UC-19 Exception Flow 1.2: Not eligible
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getDouble("min_purchase")).thenReturn(100.0);

        double discount = bookingService.getPromoDiscount("BIGSPENDER", 50.0);
        assertEquals(0.0, discount);
    }

    // --- UC-23: Cancel Booking ---
    @Test
    void testCancelBooking_Success() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("status")).thenReturn("CONFIRMED");
        when(resultSet.getDouble("total_price")).thenReturn(40.0);

        String result = bookingService.cancelBooking(101);

        assertTrue(result.contains("SUCCESS"));
        assertTrue(result.contains("Refund"));
        verify(connection).commit();
    }

    // --- UC-2/UC-24: View Bookings ---
    @Test
    void testListUserBookings_NoBookings() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        bookingService.listUserBookings("user_empty");
        verify(preparedStatement).setString(1, "user_empty");
    }
    
    @Test
    void testGetSeatsByBooking_Success() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("seat_number")).thenReturn(15, 16);

        List<Integer> seats = bookingService.getSeatsByBooking(500);

        assertEquals(2, seats.size());
        assertTrue(seats.contains(15));
    }

    @Test
    void testUpdateBookingTotal_Success() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = bookingService.updateBookingTotal(500, 150.0, "SAVE50");

        assertTrue(result);
        verify(preparedStatement).setDouble(1, 150.0);
        verify(preparedStatement).setString(2, "SAVE50");
    }

    @Test
    void testGetBookingDetails_NotFound() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        bookingService.getBookingDetails(999);

        verify(preparedStatement).executeQuery();
    }
}