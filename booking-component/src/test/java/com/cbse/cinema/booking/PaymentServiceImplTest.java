package com.cbse.cinema.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cbse.cinema.api.service.DatabaseService;

public class PaymentServiceImplTest {

    @Mock private DatabaseService dbService;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbService.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    // --- UC-20: Make Payment ---
    @Test
    void testProcessCardPayment_Success() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1); // 1st for booking, 2nd for seats

        boolean result = paymentService.processCardPayment(101, "1234567890123456", "12/26", "123");

        assertTrue(result);
        verify(preparedStatement, atLeast(2)).executeUpdate();
        // Verify UC-20: Payment status is set to CONFIRMED for cards
        verify(preparedStatement).setString(eq(1), eq("CONFIRMED"));
    }

    @Test
    void testProcessCardPayment_InvalidCard() {
        // UC-20 Exception Flow 2.1: Validation fails
        boolean result = paymentService.processCardPayment(101, "123", "12/26", "123");
        assertFalse(result);
    }

    @Test
    void testProcessOtherPayment_EWallet() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = paymentService.processOtherPayment(101, "E-Wallet");

        assertTrue(result);
        verify(preparedStatement).setString(eq(2), eq("E-Wallet"));
    }

    @Test
    void testConfirmSeatStatus_Success() throws Exception {
        // We need 2 executeUpdate calls: 1 for finalizeTransaction and 1 for confirmSeatStatus
        when(preparedStatement.executeUpdate()).thenReturn(1); 

        boolean result = paymentService.processOtherPayment(200, "Card");

        assertTrue(result);
        
        verify(connection).prepareStatement(contains("UPDATE seats SET status = 'BOOKED'"));
        verify(preparedStatement, atLeast(2)).executeUpdate();
    }

    @Test
    void testConfirmSeatStatus_Fails() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1, 0); 

        boolean result = paymentService.processOtherPayment(300, "e-wallet");

        assertFalse(result, "Payment should fail if seats cannot be confirmed as BOOKED");
    }

    // --- UC-21 & UC-22: Confirmation & QR Ticket ---
    @Test
    void testDisplayConfirmation_Success() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("booking_ref")).thenReturn("REF-CA-101-9999");
        when(resultSet.getString("qrdata")).thenReturn("TICKET|REF-CA-101|12345");
        when(resultSet.getTimestamp("paid_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

        paymentService.displayConfirmation(101);

        verify(preparedStatement).executeQuery();
        verify(resultSet).getString("qrdata");
    }

    // --- Exception Flow: Database failure during finalization ---
    @Test
    void testFinalizeTransaction_DatabaseError() throws Exception {
        // Force SQL error
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB Down"));

        boolean result = paymentService.processOtherPayment(101, "Cash");

        assertFalse(result);
    }
}