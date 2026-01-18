package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.BookingService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;


@Command(scope = "cinema", name = "book-cancel", description = "Cancel a booking (UC-23)")
@Service
public class CancelBookingCommand implements Action {

    @Reference
    private BookingService bookingService;

    @Argument(index = 0, name = "bookingId", description = "ID of the booking to cancel", required = true)
    private int bookingId;

    @Override
    public Object execute() throws Exception {
        System.out.println("Requesting cancellation for Booking #" + bookingId + "...");
        
        String result = bookingService.cancelBooking(bookingId);
        
        if (result.startsWith("SUCCESS")) {
            System.out.println("\n----------------------------------------");
            System.out.println("       CANCELLATION CONFIRMED");
            System.out.println("----------------------------------------");
            System.out.println(result);
            System.out.println("Seats have been released back to inventory.");
            System.out.println("----------------------------------------\n");
        } else {
            System.out.println("\n[!] " + result + "\n");
        }
        return null;
    }
}