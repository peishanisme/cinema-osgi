package com.cbse.cinema.recommendation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;
import com.cbse.cinema.api.service.DatabaseService;
import com.cbse.cinema.api.service.MovieService;

public class RecommendationServiceImplTest {

    @Mock
    private DatabaseService dbService;
    @Mock
    private MovieService movieService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbService.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testGetRecommendedSeats_FindsBestAvailable() throws Exception {
        // Arrange: Mock occupied seats (assume seat 33 is taken)
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("seat_number")).thenReturn(33); // Row 4, Col 1 (32+1)

        // Act: Request 2 seats
        List<Integer> recommended = recommendationService.getRecommendedSeats(2, "session123");

        assertNotNull(recommended);
        assertEquals(2, recommended.size());
        // Logic should pick center of Row 4 (the top preference) if available
        assertTrue(recommended.get(0) >= 32 && recommended.get(0) < 40);
    }

    @Test
    void testGetRecommendations_WithUserPreferences() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("movieid")).thenReturn("m1");
        when(resultSet.getString("title")).thenReturn("Inception");

        List<String> recs = recommendationService.getRecommendations("user-uuid");

        assertFalse(recs.isEmpty());
        assertTrue(recs.get(0).contains("Inception"));
        verify(preparedStatement).setString(1, "user-uuid");
    }

    @Test
    void testGetRecommendations_FallbackToPopular() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        ResultSet fallbackRs = mock(ResultSet.class);
        PreparedStatement fallbackPstmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("ORDER BY length DESC"))).thenReturn(fallbackPstmt);
        when(fallbackPstmt.executeQuery()).thenReturn(fallbackRs);
        when(fallbackRs.next()).thenReturn(true, false);
        when(fallbackRs.getString("title")).thenReturn("Long Movie");

        List<String> recs = recommendationService.getRecommendations("user-uuid");

        assertFalse(recs.isEmpty());
        assertTrue(recs.get(0).contains("Long Movie"));
    }

    @Test
    void testGetRecommendedSessions() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("sessionid")).thenReturn("s1");
        when(resultSet.getDate("show_date")).thenReturn(Date.valueOf("2026-01-20"));
        when(resultSet.getTime("show_time")).thenReturn(Time.valueOf("20:00:00"));

        List<Session> sessions = recommendationService.getRecommendedSessions("m1");

        assertEquals(1, sessions.size());
        assertEquals("s1", sessions.get(0).getSessionid());
    }

    @Test
    void testGetRecommendedMovieDetails() {
        Movie mockMovie = new Movie();
        mockMovie.setMovieid("m1");
        mockMovie.setTitle("Inception");
        
        try {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("movieid")).thenReturn("m1");
            when(resultSet.getString("title")).thenReturn("Inception");
            
            when(movieService.getMovieById("m1")).thenReturn(mockMovie);
        } catch (SQLException e) {}

        List<Movie> details = recommendationService.getRecommendedMovieDetails("user1");

        assertFalse(details.isEmpty());
        assertEquals("Inception", details.get(0).getTitle());
    }

    @Test
    void testGetRecommendedSeats_NoneAvailable() throws Exception {
        // Arrange: Mock the result set to return 64 occupied seats
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        // This creates an array of 64 'true' values followed by one 'false'
        Boolean[] sixtyFourSeats = new Boolean[65];
        java.util.Arrays.fill(sixtyFourSeats, 0, 64, true);
        sixtyFourSeats[64] = false;
        
        when(resultSet.next()).thenReturn(sixtyFourSeats[0], 
            java.util.Arrays.copyOfRange(sixtyFourSeats, 1, 65));

        // Mock rs.getInt to return seat numbers 1 to 64
        org.mockito.stubbing.Answer<Integer> seatNumberAnswer = new org.mockito.stubbing.Answer<Integer>() {
            private int count = 1;
            public Integer answer(org.mockito.invocation.InvocationOnMock invocation) {
                return count++;
            }
        };
        when(resultSet.getInt("seat_number")).thenAnswer(seatNumberAnswer);

        // Act: Request 1 seat in a full theater
        List<Integer> result = recommendationService.getRecommendedSeats(1, "sessionFull");

        // Assert: Since every seat is occupied, it should return null
        assertNull(result, "Should return null when theater is full");
    }

    @Test
    void testGetRecommendedSeats_SQLException() throws Exception {
        when(dbService.getConnection()).thenThrow(new SQLException("Database error"));

        List<Integer> result = recommendationService.getRecommendedSeats(1, "sessionError");

        org.junit.jupiter.api.Assertions.assertNull(result);
    }

    @Test
    void testGetRecommendations_DatabaseError() throws Exception {
        when(dbService.getConnection()).thenThrow(new SQLException("Conn failure"));

        List<String> results = recommendationService.getRecommendations("userError");

        assertTrue(results.isEmpty());
    }
}