package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.BookingService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.List;

@Command(scope = "cinema", name = "book-create", description = "Calculate price and create a tentative booking (UC-17 & UC-18)")
@Service
public class CreateBookingCommand implements Action {

    @Reference
    private BookingService bookingService;

    @Argument(index = 0, name = "userId", description = "The ID of the registered user", required = true)
    private String userId;

    @Argument(index = 1, name = "sessionId", description = "The ID of the movie session", required = true)
    private String sessionId;

    @Argument(index = 2, name = "seats", description = "List of seat numbers (e.g., 27 28)", multiValued = true, required = true)
    private List<Integer> seats;

    @Override
    public Object execute() throws Exception {
        // 1. UC-18: Calculate the final total (Discount is 0.0 because promo isn't applied yet)
        double finalTotal = bookingService.calculateFinalTotal(seats, 0.0);
        
        if (finalTotal <= 0) {
            System.out.println("Error: Could not calculate pricing. Please check seat selection.");
            return null;
        }

        // 2. UC-17: Create the booking record with the calculated total
        int bookingId = bookingService.createBooking(userId, sessionId, seats, finalTotal, null);

        if (bookingId > 0) {
            System.out.println("\n========================================");
            System.out.println("           BOOKING CONFIRMATION");
            System.out.println("========================================");
            System.out.println("Booking ID   : #" + bookingId);
            System.out.println("User ID      : " + userId);
            System.out.println("Status       : PENDING (Locked)");
            System.out.println(String.format("Final Total  : RM %.2f", finalTotal));
            System.out.println("========================================\n");
        } else {
            System.out.println("FAILED: Could not create booking. One or more seats are no longer available.");
        }

        return null;
    }
}