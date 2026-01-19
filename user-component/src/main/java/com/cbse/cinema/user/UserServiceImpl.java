package com.cbse.cinema.user;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.cbse.cinema.api.model.User;
import com.cbse.cinema.api.service.DatabaseService;
import com.cbse.cinema.api.service.UserService;

@Component(service = UserService.class)

public class UserServiceImpl implements UserService {

    @Reference
    private DatabaseService dbService; 

    @Override
    public void registerUser(User user) {
        String sql = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Convert String ID to UUID object for Supabase
            pstmt.setObject(1, UUID.fromString(user.getId()));
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            
            pstmt.executeUpdate();
            System.out.println("User registered in Supabase: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, UUID.fromString(id));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateUser(User updatedUser) {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, updatedUser.getUsername());
            pstmt.setString(2, updatedUser.getEmail());
            pstmt.setObject(3, UUID.fromString(updatedUser.getId()));
            
            pstmt.executeUpdate();
            System.out.println("User updated in Supabase: " + updatedUser.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to keep code clean
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    }

    @Override
    public void updateGenres(String userId, List<String> genres) {
        String sql = "UPDATE users SET genres = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Convert Java List to SQL Array
            Array genreArray = conn.createArrayOf("text", genres.toArray());
            
            pstmt.setArray(1, genreArray);
            pstmt.setObject(2, UUID.fromString(userId));
            
            pstmt.executeUpdate();
            System.out.println("Genres updated in Supabase.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addFavorite(String userId, String movieId) {
        String sql = "UPDATE users SET favorites = array_append(favorites, ?) " +
                    "WHERE id = ? AND NOT (? = ANY(favorites))";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            pstmt.setObject(2, java.util.UUID.fromString(userId));
            pstmt.setString(3, movieId); 
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Movie " + movieId + " added to favorites for user " + userId);
            } else {
                System.out.println("Movie already in favorites or user not found.");
            }
        } catch (SQLException e) {
            System.err.println("Database error adding favorite: " + e.getMessage());
        }
    }
}