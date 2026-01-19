package com.cbse.cinema.recommendation;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;
import com.cbse.cinema.api.model.Seat;
import com.cbse.cinema.api.service.RecommendationService;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.service.DatabaseService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component(service = RecommendationService.class)
public class RecommendationServiceImpl implements RecommendationService {

    @Reference
    private DatabaseService dbService;
    @Reference
    private MovieService movieService;

    private Session mapResultSetToSession(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionid(rs.getString("sessionid"));
        s.setMovieid(rs.getString("movieid")); 
        s.setShowDate(rs.getDate("show_date").toString());
        s.setShowTime(rs.getTime("show_time").toString());
        s.setHallName(rs.getString("hall_name"));
        s.setLanguage(rs.getString("language"));
        
        s.setTotalSeats(rs.getInt("calculated_total"));
        s.setAvailableSeats(rs.getInt("calculated_available"));
        
        return s;
    }

    @Override
    public List<Integer> getRecommendedSeats(int numSeats, String sessionId) {
        int[] ROW_PREFERENCE = {4, 3, 5, 2, 6, 1, 7, 0};
        int COLUMNS = 8;

        // 1. Get current occupied seats for this session
        List<Integer> occupiedSeats = new ArrayList<>();
        String sql = "SELECT seat_number FROM seats WHERE session_id = ? AND (status = 'BOOKED' OR status = 'LOCKED')";
        
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Subtract 1 if your DB uses 1-64 but logic uses 0-63
                occupiedSeats.add(rs.getInt("seat_number") - 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // 2. Logic to find the best row based on preference
        for (int row : ROW_PREFERENCE) {
            int rowStart = row * COLUMNS;
            List<Integer> bestSeatsInRow = null;
            double minOffset = Double.MAX_VALUE;

            for (int i = 0; i <= COLUMNS - numSeats; i++) {
                List<Integer> potentialSlice = new ArrayList<>();
                boolean allAvailable = true;

                for (int k = 0; k < numSeats; k++) {
                    int seatIdx = rowStart + i + k;
                    if (occupiedSeats.contains(seatIdx)) {
                        allAvailable = false;
                        break;
                    }
                    potentialSlice.add(seatIdx);
                }

                if (allAvailable) {
                    double selectionCenter = i + (numSeats - 1) / 2.0;
                    double theaterCenter = (COLUMNS - 1) / 2.0;
                    double offset = Math.abs(selectionCenter - theaterCenter);

                    if (offset < minOffset) {
                        minOffset = offset;
                        bestSeatsInRow = potentialSlice;
                    }
                }
            }

            if (bestSeatsInRow != null) {
                return bestSeatsInRow;
            }
        }
        return null; 

    }

    @Override
    public List<String> getRecommendations(String userId) {
        List<String> recommendedIds = new ArrayList<>();

        String sql = "SELECT m.movieid, m.title FROM movies m " +
             "WHERE m.genre = ANY ( " +
             "  SELECT unnest(genres) " +
             "  FROM users WHERE id = CAST(? AS UUID) " + 
             ") " +
             "AND m.movieid NOT IN ( " +
             "  SELECT s.movieid FROM bookings b " +
             "  JOIN sessions s ON CAST(b.session_id AS TEXT) = CAST(s.sessionid AS TEXT) " +
             "  WHERE CAST(b.user_id AS TEXT) = CAST(? AS TEXT) " + 
             ") " +
             "LIMIT 5";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recommendedIds.add(rs.getString("movieid") + " - " + rs.getString("title"));
                }
            }
            
            // Fallback for empty results
            if (recommendedIds.isEmpty()) {
                String fallbackSql = "SELECT movieid, title FROM movies ORDER BY length DESC LIMIT 3";
                try (PreparedStatement fPstmt = conn.prepareStatement(fallbackSql);
                    ResultSet fRs = fPstmt.executeQuery()) {
                    while (fRs.next()) {
                        recommendedIds.add(fRs.getString("movieid") + " - " + fRs.getString("title"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendedIds;
    }

    @Override
    public List<Session> getRecommendedSessions(String movieId) {
        List<Session> recommended = new ArrayList<>();
        
        String sql = "SELECT s.*, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid) as calculated_total, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid AND status = 'AVAILABLE') as calculated_available " +
                    "FROM sessions s " +
                    "WHERE s.movieid = ? " +
                    "ORDER BY calculated_available DESC " +
                    "LIMIT 3";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                recommended.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error in Recommended Sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return recommended;
    }

    @Override
    public List<Movie> getRecommendedMovieDetails(String userId) {
        List<Movie> recommendedMovies = new ArrayList<>();
        
        // 1. Get the list of recommended ID - Title strings from your existing logic
        List<String> recStrings = getRecommendations(userId);
        
        for (String entry : recStrings) {
            String movieId = entry.split(" - ")[0].trim();
            
            Movie movie = movieService.getMovieById(movieId);
            if (movie != null) {
                recommendedMovies.add(movie);
            }
        }
        return recommendedMovies;
    }
}
