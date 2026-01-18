package com.cbse.cinema.commands;

import com.cbse.cinema.api.model.Seat;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.List;

@Command(scope = "cinema", name = "seat-map", description = "Display the 8x8 seat layout (UC-13)")
@Service
public class ShowSeatMapCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", description = "The ID of the movie session", required = true)
    private String sessionId;

    @Override
    public Object execute() throws Exception {
        List<Seat> seats = movieService.getSeatLayout(sessionId);

        if (seats == null || seats.isEmpty()) {
            System.out.println("No seat layout found for session: " + sessionId);
            return null;
        }

        System.out.println("\n      ========= SCREEN =========");
        System.out.println("Legend: [P]=Premium [V]=VIP [C]=Couple [N]=Normal [X]=Booked [L]=Locked\n");

        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            String icon;

            // Check the status first (BOOKED or LOCKED)
            if ("BOOKED".equalsIgnoreCase(s.getStatus())) {
                icon = "[ X ]";
            } else if ("LOCKED".equalsIgnoreCase(s.getStatus())) {
                icon = "[ L ]";
            } else {
                // If not booked or locked, show the type of seat available
                String type = (s.getType() != null) ? s.getType().toUpperCase() : "NORMAL";
                switch (type) {
                    case "VIP":
                        icon = "[ V ]";
                        break;
                    case "COUPLE":
                        icon = "[ C ]";
                        break;
                    case "PREMIUM":
                        icon = "[ P ]";
                        break;
                    default:
                        icon = "[ N ]";
                        break;
                }
            }

            System.out.print(icon + " ");

            // Your logic for 8x8 grid (Line break every 8 seats)
            if ((i + 1) % 8 == 0) {
                System.out.println();
            }
        }
        System.out.println("\n================================\n");
        return null;
    }
}