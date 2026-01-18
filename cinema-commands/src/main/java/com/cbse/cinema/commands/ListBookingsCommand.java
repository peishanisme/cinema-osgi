package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.BookingService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "cinema", name = "user-bookings", description = "View your booking history (UC-2)")
@Service
public class ListBookingsCommand implements Action {

    @Reference
    private BookingService bookingService;

    @Argument(index = 0, name = "userId", description = "Your User UUID", required = true)
    private String userId;

    @Override
    public Object execute() throws Exception {
        bookingService.listUserBookings(userId);
        return null;
    }
}