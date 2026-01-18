package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.model.Movie;

// uc5 - list movie listings
@Command(scope = "cinema", name = "movie-list", description = "View Movie Listings (UC-5)")
@Service
public class ListMoviesCommand implements Action {

    @Reference
    private MovieService movieService;

    @Override
    public Object execute() throws Exception {
        List<Movie> movies = movieService.getAllMovies();

        if (movies.isEmpty()) {
            System.out.println("Error: Movie listings not fetched from database.");
            return null;
        }

        System.out.println(String.format("%-10s | %-25s | %-15s", "ID", "Title", "Genre"));
        System.out.println("------------------------------------------------------------");
        for (Movie m : movies) {
            System.out.println(String.format("%-10s | %-25s | %-15s", 
                m.getMovieid(), m.getTitle(), m.getGenre()));
        }
        return null;
    }
}