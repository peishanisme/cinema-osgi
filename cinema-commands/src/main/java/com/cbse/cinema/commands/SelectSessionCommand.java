package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.model.Session;
import org.apache.karaf.shell.api.action.Argument;

// uc11 - show session details
@Command(scope = "cinema", name = "session-select", description = "Select a specific session (UC-11)")
@Service
public class SelectSessionCommand implements Action {

    @Reference
    private MovieService movieService;

    @Argument(index = 0, name = "sessionId", description = "The ID of the session", required = true)
    private String sessionId;

    @Override
    public Object execute() throws Exception {
        Session session = movieService.getSessionById(sessionId);

        if (session == null) {
            System.out.println("Error: Session ID " + sessionId + " not found.");
            return null;
        }

        System.out.println("\n--- SESSION SELECTED ---");
        System.out.println("Session ID : " + session.getSessionid());
        System.out.println("Time       : " + session.getShowDate() + " at " + session.getShowTime());
        System.out.println("Language   : " + session.getLanguage());
        System.out.println("Hall       : " + session.getHallName());
        System.out.println("Proceeding to seat selection...\n");
        
        return null;
    }
}
