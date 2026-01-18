package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Argument;

@Command(scope = "cinema", name = "seat-recommend", description = "Get recommended seats (UC-14)")
@Service
public class RecommendSeatCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", required = true)
    private String sessionId;

    @Argument(index = 1, name = "numSeats", description = "Number of seats needed", required = false)
    private int numSeats = 1; 

    @Override
    public Object execute() throws Exception {
        List<Integer> recommended = movieService.getRecommendedSeats(numSeats, sessionId);

        if (recommended == null) {
            System.out.println("No suitable available seats found for " + numSeats + " customers.");
            return null;
        }

        System.out.println("\n--- SEAT RECOMMENDATION ---");
        System.out.print("Recommended Seats: ");
        for (Integer sIdx : recommended) {
            // Adding 1 to return to 1-64 human-readable format
            System.out.print((sIdx + 1) + " ");
        }
        System.out.println("\n---------------------------\n");
        return null;
    }
}
