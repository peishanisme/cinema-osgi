package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.List;

//uc26 - select recommended seat
@Command(scope = "cinema", name = "seat-select-rec", description = "Automatically recommend and lock seats in one step (UC-26)")
@Service
public class SelectRecommendedSeatCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", description = "The session UUID", required = true)
    private String sessionId;

    @Argument(index = 1, name = "numSeats", description = "Number of seats the user wants to book", required = true)
    private int numSeats;

    @Argument(index = 2, name = "userId", description = "The User UUID", required = true)
    private String userId;

    @Override
    public Object execute() throws Exception {
        System.out.println("Fetching recommendations for " + numSeats + " seats...");

        // 1. Call getRecommendedSeats to get the best available block
        List<Integer> recommendedSeats = movieService.getRecommendedSeats(numSeats, sessionId);

        if (recommendedSeats == null || recommendedSeats.isEmpty()) {
            System.out.println("\n[!] Error: No suitable block of " + numSeats + " seats could be recommended.");
            return null;
        }

        System.out.println("System suggests seats: " + recommendedSeats);
        System.out.println("Locking these seats for User " + userId + "...");

        // 2. Pass the recommended list directly into the lockSeats method
        boolean isLocked = movieService.lockSeats(sessionId, recommendedSeats);

        if (isLocked) {
            System.out.println("\n------------------------------------------------");
            System.out.println("   [SUCCESS] SEATS RESERVED SUCCESSFULLY");
            System.out.println("------------------------------------------------");
            System.out.println("Locked Seats : " + recommendedSeats);
            System.out.println("Session ID   : " + sessionId);
            System.out.println("\nNext: Use 'cinema:book-pay' to complete payment.");
            System.out.println("------------------------------------------------\n");
        } else {
            System.out.println("\n[!] Error: Recommended seats could not be locked.");
            System.out.println("They might have been taken by another user just now. Please try again.");
        }

        return null;
    }
}