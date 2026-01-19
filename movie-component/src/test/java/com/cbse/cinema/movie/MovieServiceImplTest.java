package com.cbse.cinema.movie;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Seat;
import com.cbse.cinema.api.model.Session;
import com.cbse.cinema.api.service.DatabaseService;

public class MovieServiceImplTest {

    @Mock
    private DatabaseService dbService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private Statement statement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private MovieServiceImpl movieService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbService.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testGetAllMovies() throws Exception {
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("title")).thenReturn("Inception");

        List<Movie> movies = movieService.getAllMovies();

        assertFalse(movies.isEmpty());
        assertEquals("Inception", movies.get(0).getTitle());
    }

    @Test
    void testSearchMoviesByName() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("title")).thenReturn("Avatar");

        List<Movie> results = movieService.searchMoviesByName("Ava");

        assertEquals(1, results.size());
        verify(preparedStatement).setString(eq(1), contains("Ava"));
    }

    @Test
    void testGetMovieById() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("movieid")).thenReturn("m1");

        Movie movie = movieService.getMovieById("m1");

        assertNotNull(movie);
        assertEquals("m1", movie.getMovieid());
    }

    @Test
    void testGetMovieById_NotFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Simulate no record found

        // Act
        Movie result = movieService.getMovieById("invalid_id");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetMoviesByGenre() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("genre")).thenReturn("Action");

        List<Movie> results = movieService.getMoviesByGenre("Action");

        assertEquals(1, results.size());
        assertEquals("Action", results.get(0).getGenre());
    }

    @Test
    void testGetSessionsByMovie() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("sessionid")).thenReturn("s1");
        // Mocking SQL Date and Time for the mapper
        when(resultSet.getDate("show_date")).thenReturn(Date.valueOf("2026-01-20"));
        when(resultSet.getTime("show_time")).thenReturn(Time.valueOf("20:00:00"));

        List<Session> sessions = movieService.getSessionsByMovie("m1");

        assertFalse(sessions.isEmpty());
        assertEquals("s1", sessions.get(0).getSessionid());
    }

    @Test
    void testGetSeatLayout() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("seat_number")).thenReturn(5);
        when(resultSet.getString("status")).thenReturn("AVAILABLE");

        List<Seat> layout = movieService.getSeatLayout("s1");

        assertEquals(1, layout.size());
        assertEquals(5, layout.get(0).getSeatNumber());
    }

    @Test
    void testLockSeats_Success() throws Exception {
        List<Integer> seats = List.of(1, 2);
        when(preparedStatement.executeUpdate()).thenReturn(2); // Match list size

        boolean result = movieService.lockSeats("u1", "s1", seats);

        assertTrue(result);
        verify(preparedStatement).setString(1, "u1");
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setInt(4, 2);
    }

    @Test
    void testLockSeats_AlreadyTaken() throws Exception {
        // Arrange: We want 2 seats, but DB only updates 1 (because 1 is already LOCKED)
        List<Integer> seats = List.of(1, 2);
        when(preparedStatement.executeUpdate()).thenReturn(1); 

        // Act
        boolean result = movieService.lockSeats("u1", "s1", seats);

        // Assert
        assertFalse(result); // Logic: 1 != 2, so it must return false
    }

    @Test
    void testLockSeats_DatabaseError() throws Exception {
        // Arrange
        List<Integer> seats = List.of(1);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Conn error"));

        // Act
        boolean result = movieService.lockSeats("u1", "s1", seats);

        // Assert
        assertFalse(result); // Should return false per catch block logic
    }

    @Test
    void testReleaseSeats_Success() throws Exception {
        List<Integer> seats = List.of(10);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = movieService.releaseSeats("u1", "s1", seats);

        assertTrue(result);
        verify(preparedStatement).setString(1, "s1"); 
        verify(preparedStatement).setString(2, "u1"); 
        verify(preparedStatement).setInt(3, 10);      
    }

    @Test
    void testReleaseSeats_EmptyList() {
        // Act & Assert
        // Should return false immediately without even calling the database
        assertFalse(movieService.releaseSeats("u1", "s1", null));
        assertFalse(movieService.releaseSeats("u1", "s1", List.of()));
    }

    @Test
    void testDisplayMovieDetails() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("title")).thenReturn("Batman");

        movieService.displayMovieDetails("m1");

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testDisplayMovieDetails_NotFound() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        movieService.displayMovieDetails("invalid-id");

        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetAllMovies_DatabaseError() throws Exception {
        // Arrange: Force the statement to throw an exception
        when(statement.executeQuery(anyString())).thenThrow(new SQLException("Database Down"));

        // Act
        List<Movie> movies = movieService.getAllMovies();

        // Assert
        assertTrue(movies.isEmpty()); // Should return an empty list instead of crashing
    }

}