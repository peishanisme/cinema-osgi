package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.BookingService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "cinema", name = "book-details", description = "View full booking details (UC-24)")
@Service
public class ViewBookingCommand implements Action {

    @Reference
    private BookingService bookingService;

    @Argument(index = 0, name = "bookingId", description = "ID of the booking to view", required = true)
    private int bookingId;

    @Override
    public Object execute() throws Exception {
        bookingService.getBookingDetails(bookingId);
        return null;
    }
}