package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;

import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.User;
import com.cbse.cinema.api.service.UserService;

@Command(scope = "cinema", name = "register", description = "Register a new user")
@Service
public class RegisterCommand implements Action {

    @Reference
    private UserService userService;

    // index 0 is the first word after the command
    @Argument(index = 0, name = "username", description = "User's name", required = true)
    private String username;

    @Argument(index = 1, name = "email", description = "User's email", required = true)
    private String email;

    @Argument(index = 2, name = "password", description = "User's password", required = true)
    private String password;

    @Override
    public Object execute() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); 

        userService.registerUser(user);
        System.out.println("Successfully registered " + username + " (" + email + ")");
        return null;
    }
}