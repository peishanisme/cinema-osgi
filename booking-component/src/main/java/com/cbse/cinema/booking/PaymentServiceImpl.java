package com.cbse.cinema.booking;

import java.sql.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.cbse.cinema.api.service.DatabaseService;
import com.cbse.cinema.api.service.PaymentService;

@Component(service = PaymentService.class)
public class PaymentServiceImpl implements PaymentService {

    @Reference
    private DatabaseService dbService;

    @Override
    public boolean processCardPayment(int bookingId, String cardNum, String expiry, String cvv) {
        if (cardNum == null || cardNum.length() < 16) return false;
        return finalizeTransaction(bookingId, "Card");
    }

    @Override
    public boolean processOtherPayment(int bookingId, String method) {
        return finalizeTransaction(bookingId, method);
    }

    /**
     * Internal helper to handle the shared logic of generating Ref/QR and updating DB.
     */
    private boolean finalizeTransaction(int bookingId, String method) {
        String finalStatus = method.equalsIgnoreCase("e-wallet") || method.equalsIgnoreCase("card") 
                             ? "CONFIRMED" : "PENDING_COLLECTION";
        
        // 1. Generate unique Reference & QR Data (UC-21)
        String ref = "REF-" + method.substring(0,2).toUpperCase() + "-" + bookingId + "-" + (int)(Math.random() * 9000 + 1000);
        String qr = "TICKET|" + ref + "|" + System.currentTimeMillis();

        // 2. Update Booking with Ref, QR, and Timestamp
        String sql = "UPDATE bookings SET status = ?, payment_method = ?, booking_ref = ?, qrdata = ?, paid_at = CURRENT_TIMESTAMP WHERE booking_id = ?";
        
        try (Connection conn = dbService.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, finalStatus);
            pstmt.setString(2, method);
            pstmt.setString(3, ref);
            pstmt.setString(4, qr);
            pstmt.setInt(5, bookingId);
            
            if (pstmt.executeUpdate() > 0) {
                return confirmSeatStatus(bookingId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean confirmSeatStatus(int bookingId) {
        String sql = "UPDATE seats SET status = 'BOOKED' WHERE seat_number IN " +
                     "(SELECT seat_number FROM booking_seats WHERE booking_id = ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public void displayConfirmation(int bookingId) {
        String sql = "SELECT b.booking_ref, b.qrdata, b.total_price, b.payment_method, b.paid_at, " +
                     "array_agg(bs.seat_number) as seats " +
                     "FROM bookings b " +
                     "JOIN booking_seats bs ON b.booking_id = bs.booking_id " +
                     "WHERE b.booking_id = ? " +
                     "GROUP BY b.booking_id, b.booking_ref, b.qrdata, b.total_price, b.payment_method, b.paid_at";

        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n************************************************");
                System.out.println("           MOVIE TICKET CONFIRMATION            ");
                System.out.println("************************************************");
                System.out.println("Booking Ref : " + rs.getString("booking_ref"));
                System.out.println("Date/Time   : " + rs.getTimestamp("paid_at"));
                System.out.println("Seats       : " + rs.getString("seats"));
                System.out.println("Total Paid  : RM " + String.format("%.2f", rs.getDouble("total_price")));
                System.out.println("Payment     : " + rs.getString("payment_method"));
                System.out.println("------------------------------------------------");
                System.out.println("       [ QR CODE: " + rs.getString("qrdata") + " ]       ");
                System.out.println("      (Present this at the Cinema Hall Entrance)  ");
                System.out.println("************************************************\n");
            }
        } catch (SQLException e) {
            System.err.println("Error: Could not generate confirmation.");
        }
    }
}