package com.cbse.cinema.commands;

import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.List;

@Command(scope = "cinema", name = "recommend", description = "View recommended movies based on preferences (UC-25)")
@Service
public class RecommendCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "userId", description = "The ID of the user", required = true)
    private String userId;

    @Argument(index = 1, name = "movieId", description = "Optional: Select a movie ID to view full details", required = false)
    private String movieId;

    @Override
    public Object execute() throws Exception {
        if (movieId == null || movieId.isEmpty()) {
            // FLOW: Dashboard List
            System.out.println("\nCalculating recommendations for User: " + userId + "...");
            List<String> recommendations = movieService.getRecommendations(userId);

            if (recommendations.isEmpty()) {
                System.out.println("No movies found matching your preferences. Check back later!");
            } else {
                System.out.println("\n--- RECOMMENDED FOR YOU ---");
                for (String rec : recommendations) {
                    System.out.println(" > " + rec);
                }
                System.out.println("\nTo view details, type: cinema:recommend " + userId + " [MovieID]");
            }
        } else {
            // FLOW: Detailed Movie View
            movieService.displayMovieDetails(movieId);
        }
        return null;
    }
}