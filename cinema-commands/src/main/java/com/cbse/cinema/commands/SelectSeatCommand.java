package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Command;
import java.util.List;

// uc16 - select and lock seat
@Command(scope = "cinema", name = "seat-select", description = "Select and lock multiple seats (UC-16)")
@Service
public class SelectSeatCommand implements Action {

    @Reference
    private MovieService movieService;

     @Argument(index = 0, name = "userId", description = "User UUID", required = true)
    private String userId;
    
    @Argument(index = 1, name = "sessionId", description = "Session UUID", required = true)
    private String sessionId;

    @Argument(index = 2, name = "seatNumbers", multiValued = true, required = true)
    private List<Integer> seatNumbers;

    @Override
    public Object execute() throws Exception {
        System.out.println("Attempting to lock seats " + seatNumbers + " for User: " + userId);
        
        // Passing the userId to your service logic
        boolean success = movieService.lockSeats(userId, sessionId, seatNumbers);

        if (success) {
            System.out.println("\n[SUCCESS] Seats " + seatNumbers + " are now LOCKED.");
            System.out.println("These seats are reserved for your account.");
            System.out.println("Next: Proceed to payment to finalize booking.");
        } else {
            System.out.println("\n[ERROR] Selection failed. One or more seats are already taken or locked.");
        }
        return null;
    }
}