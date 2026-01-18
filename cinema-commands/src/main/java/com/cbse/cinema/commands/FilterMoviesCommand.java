package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.service.MovieService;
import java.util.List;

// uc8 - filter movies by genre
@Command(scope = "cinema", name = "movie-filter", description = "Filter movies by genre (UC-8)")
@Service
public class FilterMoviesCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "genre", description = "The genre to filter by", required = true)
    private String genre;

    @Override
    public Object execute() throws Exception {
        List<Movie> results = movieService.getMoviesByGenre(genre);

        if (results.isEmpty()) {
            System.out.println("No movies found for the genre: " + genre);
            return null;
        }

        System.out.println("Displaying movies for genre: " + genre);
        System.out.println("------------------------------------------------------------");
        for (Movie m : results) {
            System.out.println(String.format("[%s] %-25s | %s mins", 
                m.getMovieid(), m.getTitle(), m.getLength()));
        }
        return null;
    }
}
