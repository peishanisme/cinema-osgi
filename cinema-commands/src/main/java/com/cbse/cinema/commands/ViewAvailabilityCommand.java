package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.model.Session;
import org.apache.karaf.shell.api.action.Argument;

// uc12 - view seat availability
@Command(scope = "cinema", name = "session-availability", description = "View seat availability (UC-12)")
@Service
public class ViewAvailabilityCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", description = "The ID of the session", required = true)
    private String sessionId;

    @Override
    public Object execute() throws Exception {
        Session session = movieService.getSessionById(sessionId);

        if (session == null) {
            System.out.println("Error: Session not found.");
            return null;
        }

        int occupied = session.getTotalSeats() - session.getAvailableSeats();
        
        System.out.println("\n--- DYNAMIC SEAT AVAILABILITY ---");
        System.out.println("Session    : " + session.getSessionid() + " (" + session.getHallName() + ")");
        System.out.println("Total Capacity: " + session.getTotalSeats());
        System.out.println("Available     : " + session.getAvailableSeats());
        System.out.println("Occupied      : " + occupied);
        
        // Simple visual representation
        if (session.getAvailableSeats() == 0) {
            System.out.println("STATUS: [SOLD OUT]");
        } else if (session.getAvailableSeats() < 10) {
            System.out.println("STATUS: [SELLING FAST]");
        } else {
            System.out.println("STATUS: [AVAILABLE]");
        }
        System.out.println("----------------------------------\n");
        
        return null;
    }
}
