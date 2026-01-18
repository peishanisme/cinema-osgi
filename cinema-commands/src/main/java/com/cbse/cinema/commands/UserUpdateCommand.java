package com.cbse.cinema.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument; 
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import com.cbse.cinema.api.model.User;
import com.cbse.cinema.api.service.UserService;

// uc1 - update account details
@Command(scope = "cinema", name = "user-update", description = "Update account details (UC-1)")
@Service
public class UserUpdateCommand implements Action {

    @Reference
    private UserService userService;

    @Argument(index = 0, name = "id", description = "User ID", required = true)
    private String id;

    @Argument(index = 1, name = "username", description = "New username", required = true)
    private String newUsername;

    @Argument(index = 2, name = "email", description = "New email", required = true)
    private String newEmail;

    @Override
    public Object execute() throws Exception {
        User user = userService.findById(id);
        
        if (user == null) {
            System.out.println("Error: User not found.");
            return null;
        }

        // Apply changes
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        
        userService.updateUser(user);
        
        // Post-condition from UC-1
        System.out.println("Updated Successfully!"); 
        return null;
    }
}