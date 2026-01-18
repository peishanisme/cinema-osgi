package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.service.MovieService;
import java.util.List;

// uc6 - search movie by name
@Command(scope = "cinema", name = "movie-search", description = "Search movies by name (UC-6)")
@Service
public class SearchMovieCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "name", description = "Movie title to search for", required = true)
    private String name;

    @Override
    public Object execute() throws Exception {
        List<Movie> results = movieService.searchMoviesByName(name);

        // Alternative flow - No movies found
        if (results.isEmpty()) {
            System.out.println("No movie name is matched or related to the movies list in the database.");
            return null;
        }

        System.out.println("Search Results for: " + name);
        System.out.println("------------------------------------------------------------");
        for (Movie m : results) {
            System.out.println(String.format("[%s] %s (%s)", m.getMovieid(), m.getTitle(), m.getGenre()));
        }
        return null;
    }
}