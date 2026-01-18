package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.service.MovieService;
import org.apache.karaf.shell.api.action.Command;

// uc7 - select movie by id
@Command(scope = "cinema", name = "movie-select", description = "Select and view movie details (UC-7)")
@Service
public class SelectMovieCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "movieId", description = "The ID of the movie to select", required = true)
    private String movieId;

    @Override
    public Object execute() throws Exception {
        Movie movie = movieService.getMovieById(movieId);

        if (movie == null) {
            System.out.println("Error: Movie with ID " + movieId + " not found.");
            return null;
        }

        System.out.println("\n--- MOVIE DETAILS ---");
        System.out.println("Title    : " + movie.getTitle());
        System.out.println("Genre    : " + movie.getGenre());
        System.out.println("Duration : " + movie.getLength() + " minutes");
        System.out.println("Synopsis : " + movie.getSynopsis());
        System.out.println("----------------------\n");
        
        return null;
    }
}
