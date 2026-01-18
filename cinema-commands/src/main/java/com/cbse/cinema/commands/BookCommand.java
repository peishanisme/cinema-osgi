package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference; // Use Karaf's Reference for Commands
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.UserService;

@Command(scope = "cinema", name = "book", description = "Book ticket")
@Service
public class BookCommand implements Action {

    // @Reference
    // private BookingService bookingService;
    @Reference
    private UserService userService;

    @Override
    public Object execute() throws Exception {

        // if (bookingService == null) {
        //     System.out.println("Error: BookingService is not available.");
        //     return null;
        // }

        // System.out.println(bookingService.book("Avatar", 2));
        return null;
    }
}