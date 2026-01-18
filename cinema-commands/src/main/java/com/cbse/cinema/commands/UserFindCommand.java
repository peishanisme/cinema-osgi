package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.User;
import com.cbse.cinema.api.service.UserService;

// uc1 - view user details
@Command(scope = "cinema", name = "user-find", description = "Find a user by their email")
@Service
public class UserFindCommand implements Action {

    @Reference
    private UserService userService;

    @Argument(index = 0, name = "email", description = "User email to search", required = true)
    private String email;

    @Override
    public Object execute() throws Exception {
        User user = userService.findByEmail(email);

        if (user == null) {
            System.out.println("No user found with email: " + email);
            return null;
        }

        System.out.println("User Found:");
        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Genres: " + user.getGenres());
        System.out.println();
        return null;
    }
}