package com.cbse.cinema.user;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cbse.cinema.api.model.User;
import com.cbse.cinema.api.service.DatabaseService;

public class UserServiceImplTest {

    @Mock
    private DatabaseService dbService;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbService.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testRegisterUser() throws Exception {
        User user = new User();
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        userService.registerUser(user);

        verify(connection).prepareStatement(contains("INSERT INTO users"));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testFindByEmail() throws Exception {
        String email = "test@example.com";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("email")).thenReturn(email);

        User foundUser = userService.findByEmail(email);

        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
    }

    @Test
    void testFindById_Success() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("id")).thenReturn(userId);
        when(resultSet.getString("username")).thenReturn("UM_Student");

        // Act
        User foundUser = userService.findById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals("UM_Student", foundUser.getUsername());
        verify(preparedStatement).setObject(eq(1), eq(UUID.fromString(userId)));
    }

    @Test
    void testUpdateUser() throws Exception {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("NewName");
        user.setEmail("new@example.com");

        // Act
        userService.updateUser(user);

        // Assert
        verify(connection).prepareStatement(contains("UPDATE users SET username"));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateUser_DatabaseError() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        // Force an error
        when(dbService.getConnection()).thenThrow(new SQLException("Database Offline"));

        // Assert that the component handles the error gracefully
        assertDoesNotThrow(() -> userService.updateUser(user));
    }

    @Test
    void testUpdateGenres() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        List<String> genres = List.of("Action", "Sci-Fi", "Drama");
        
        // Mock the Array object that JDBC returns
        Array mockSqlArray = mock(Array.class);
        when(connection.createArrayOf(eq("text"), any(Object[].class))).thenReturn(mockSqlArray);

        userService.updateGenres(userId, genres);

        verify(connection).prepareStatement(contains("UPDATE users SET genres"));
        
        verify(connection).createArrayOf(eq("text"), any(Object[].class));
        
        verify(preparedStatement).setArray(eq(1), eq(mockSqlArray));
        verify(preparedStatement).setObject(eq(2), eq(UUID.fromString(userId)));
        
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testAddFavorite_NewMovie() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String movieId = "movie123";
        when(preparedStatement.executeUpdate()).thenReturn(1); // Simulate 1 row updated

        // Act
        userService.addFavorite(userId, movieId);

        // Assert
        verify(preparedStatement).setString(1, movieId);
        verify(preparedStatement).setObject(2, UUID.fromString(userId));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testAddFavorite_AlreadyExists() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(0); 

        userService.addFavorite(UUID.randomUUID().toString(), "movie_123");

        verify(preparedStatement).executeUpdate();
    }
}