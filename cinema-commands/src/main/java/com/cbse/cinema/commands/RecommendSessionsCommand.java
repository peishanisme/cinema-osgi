package com.cbse.cinema.commands;

import com.cbse.cinema.api.model.Session;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.service.RecommendationService;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;

@Command(scope = "cinema", name = "session-recommend", description = "View recommended sessions for a movie (UC-27)")
@Service
public class RecommendSessionsCommand implements Action {

    @Reference
    private RecommendationService recommendationService;

    @Argument(index = 0, name = "movieId", description = "The ID of the movie", required = true)
    private String movieId;

    @Override
    public Object execute() throws Exception {
        System.out.println("Analyzing availability for Movie ID: " + movieId + "...");

        List<Session> recommendations = recommendationService.getRecommendedSessions(movieId);

        if (recommendations.isEmpty()) {
            System.out.println("No sessions found for this movie.");
        } else {
            System.out.println("\n--- RECOMMENDED SESSIONS (Best Availability) ---");
            System.out.println(String.format("%-15s | %-12s | %-10s | %-15s", "SESSION ID", "DATE", "TIME", "FREE SEATS"));
            System.out.println("------------------------------------------------------------------");
            
            for (Session s : recommendations) {
                System.out.println(String.format("%-15s | %-12s | %-10s | %-5d / %-5d", 
                    s.getSessionid(), 
                    s.getShowDate(), 
                    s.getShowTime(), 
                    s.getAvailableSeats(), 
                    s.getTotalSeats()));
            }
            System.out.println("------------------------------------------------------------------\n");
        }
        return null;
    }
}