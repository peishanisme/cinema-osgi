package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.DatabaseService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Command(scope = "cinema", name = "book-ticket", description = "View the QR Ticket payload for a booking (UC-22)")
@Service
public class ViewTicketCommand implements Action {

    @Reference
    private DatabaseService dbService;

    @Argument(index = 0, name = "bookingId", description = "The ID of the booking", required = true)
    private int bookingId;

    @Override
    public Object execute() throws Exception {
        String sql = "SELECT booking_ref, qrdata, status FROM bookings WHERE booking_id = ?";

        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                
                // UC-22 Pre-condition Check: Must be CONFIRMED
                if (!"CONFIRMED".equalsIgnoreCase(status) && !"PENDING_COLLECTION".equalsIgnoreCase(status)) {
                    System.out.println("[ERROR] Ticket not available. Current status: " + status);
                    return null;
                }

                String ref = rs.getString("booking_ref");
                String qrData = rs.getString("qrdata");

                System.out.println("\n------------------------------------------------");
                System.out.println("            OFFICIAL CINEMA TICKET              ");
                System.out.println("------------------------------------------------");
                System.out.println("REFERENCE : " + ref);
                System.out.println("STATUS    : " + status);
                System.out.println("\n[ ENCODED QR DATA ]");
                System.out.println(qrData != null ? qrData : "No QR Data generated yet.");
                System.out.println("\n------------------------------------------------");
                System.out.println("  Present this string/code for hall entry verification. ");
                System.out.println("------------------------------------------------\n");

            } else {
                System.out.println("No booking found with ID: " + bookingId);
            }
        }
        return null;
    }
}
