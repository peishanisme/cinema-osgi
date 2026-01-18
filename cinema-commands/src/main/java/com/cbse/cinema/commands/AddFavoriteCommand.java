package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference; 
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.service.UserService;
import org.apache.karaf.shell.api.action.Argument; 

// uc4 - add favorite movie
@Command(scope = "cinema", name = "favourite-add", description = "Add a movie to favorites (UC-4)")
@Service
public class AddFavoriteCommand implements Action {

    @Reference
    private UserService userService;

    @Argument(index = 0, name = "userId", description = "User ID", required = true)
    private String userId;

    @Argument(index = 1, name = "movieId", description = "Movie ID", required = true)
    private String movieId;

    @Override
    public Object execute() throws Exception {
        userService.addFavorite(userId, movieId);
        System.out.println("Favourites updated in dashboard."+
                           " Added movie ID: " + movieId); 
        return null;
    }
}