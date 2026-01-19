package com.cbse.cinema.commands;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.service.RecommendationService;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;

@Command(scope = "cinema", name = "view-recommended-movies", description = "View all movies with personalized recommendations (UC-28)")
@Service
public class ViewRecommendedMoviesCommand implements Action {

    @Reference
    private RecommendationService recommendationService;

    @Argument(index = 0, name = "userId", description = "User UUID for personalized recs", required = false)
    private String userId;

    @Override
    public Object execute() throws Exception {

        if (userId != null) {
            List<Movie> recs = recommendationService.getRecommendedMovieDetails(userId);
            if (!recs.isEmpty()) {
                System.out.println("\n JUST FOR YOU (Recommended) ");
                System.out.println("----------------------------------------------------------------");
                for (Movie m : recs) {
                    System.out.println(String.format("[%s] %s | Genre: %s | Synopsis: %s", m.getMovieid(), m.getTitle(), m.getGenre(), m.getSynopsis()));
                }
                System.out.println("----------------------------------------------------------------");
            }
        }

        return null;
    }
}
