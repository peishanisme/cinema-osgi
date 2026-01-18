package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.UserService;
import java.util.Arrays;
import java.util.List;

// UC3 - update genre preferences
@Command(scope = "cinema", name = "genre-update", description = "Update movie genre preferences (UC-3)")
@Service
public class GenreUpdateCommand implements Action {

    @Reference
    private UserService userService;

    @Argument(index = 0, name = "userId", description = "User ID", required = true)
    private String userId;

    @Argument(index = 1, name = "genres", description = "Comma-separated list of genres (e.g., Action,Comedy)", required = true)
    private String genresInput;

    @Override
    public Object execute() throws Exception {
        // Convert comma-separated string to List
        List<String> selectedGenres = Arrays.asList(genresInput.split(","));
        
        // Trigger the service update
        userService.updateGenres(userId, selectedGenres);
        
        System.out.println("Genre preferences stored and updated."+
                           " Selected genres: " + selectedGenres); 
        return null;
    }
}
