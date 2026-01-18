package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Command;
import java.util.List;

// uc16 alternative flow - reset seat selection
@Command(scope = "cinema", name = "seat-reset", description = "Release multiple locked seats")
@Service
public class ResetSelectionCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", required = true)
    private String sessionId;

    @Argument(index = 1, name = "seatNumbers", multiValued = true, required = true)
    private List<Integer> seatNumbers;

    @Override
    public Object execute() throws Exception {
        if (movieService.releaseSeats(sessionId, seatNumbers)) {
            System.out.println("Success: Seats " + seatNumbers + " have been released.");
        } else {
            System.out.println("Error: Could not release seats. Ensure they are currently LOCKED.");
        }
        return null;
    }
}
