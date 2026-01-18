package com.cbse.cinema.commands;

import com.cbse.cinema.api.model.Seat;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.List;

@Command(scope = "cinema", name = "seat-monitor", description = "Monitor seat availability in real-time (UC-15)")
@Service
public class RealTimeSeatMonitorCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", required = true)
    private String sessionId;

    @Override
    public Object execute() throws Exception {
        System.out.println("Monitoring Session: " + sessionId);
        System.out.println("Press Ctrl+C to stop monitoring.\n");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("\033[H\033[2J");
                System.out.flush();

                renderMap();

                Thread.sleep(3000); 
            }
        } catch (InterruptedException e) {
            System.out.println("\nReal-time monitoring stopped.");
        } catch (Exception e) {
            System.out.println("\nReal-time updates unavailable. Seat availability may not be up to date.");
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    private void renderMap() {
        List<Seat> seats = movieService.getSeatLayout(sessionId);
        System.out.println("--- REAL-TIME SEAT MAP [" + sessionId + "] ---");
        System.out.println("Last Updated: " + new java.util.Date());
        System.out.println("Legend: [O]=Available [X]=Booked\n");

        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            String icon = "BOOKED".equalsIgnoreCase(s.getStatus()) ? "[ X ]" : "[ O ]";
            System.out.print(icon + " ");
            if ((i + 1) % 8 == 0) System.out.println();
        }
        System.out.println("\nRefreshing every 3 seconds...");
    }
}