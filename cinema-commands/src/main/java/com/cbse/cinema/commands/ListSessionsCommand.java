package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import java.util.List;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.model.Session;
import org.apache.karaf.shell.api.action.Argument;

// uc9 - list sessions for a movie
@Command(scope = "cinema", name = "movie-sessions", description = "View sessions for a movie (UC-9)")
@Service
public class ListSessionsCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "movieId", description = "The ID of the movie", required = true)
    private String movieId;

    @Override
    public Object execute() throws Exception {
        List<Session> sessions = movieService.getSessionsByMovie(movieId);
        
        if (sessions.isEmpty()) {
            System.out.println("No sessions found.");
            return null;
        }

        System.out.println("Available Sessions for Movie: " + movieId);
        System.out.println(String.format("%-10s | %-12s | %-10s | %-15s | %-10s", 
            "ID", "Date", "Time", "Language", "Hall"));
        System.out.println("----------------------------------------------------------------------");
        for (Session s : sessions) {
            System.out.println(String.format("%-10s | %-12s | %-10s | %-15s | %-10s", 
                s.getSessionid(), s.getShowDate(), s.getShowTime(), s.getLanguage(), s.getHallName()));
        }
        return null;
    }
}