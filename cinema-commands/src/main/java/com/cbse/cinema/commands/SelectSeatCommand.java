package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Command;
import java.util.List;

// uc16 - select and lock seat
@Command(scope = "cinema", name = "seat-select", description = "Select multiple seats")
@Service
public class SelectSeatCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", required = true)
    private String sessionId;

    // This collects all numbers typed after the sessionId
    @Argument(index = 1, name = "seatNumbers", multiValued = true, required = true)
    private List<Integer> seatNumbers;

    @Override
    public Object execute() throws Exception {
        boolean success = movieService.lockSeats(sessionId, seatNumbers);

        if (success) {
            System.out.println("Success! Seats " + seatNumbers + " are now LOCKED.");
        } else {
            System.out.println("Error: One or more selected seats are unavailable.");
        }
        return null;
    }
}