package com.cbse.cinema.movie;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;
import com.cbse.cinema.api.model.Seat;
import com.cbse.cinema.api.service.MovieService;
import com.cbse.cinema.api.service.DatabaseService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component(service = MovieService.class)
public class MovieServiceImpl implements MovieService {

    @Reference
    private DatabaseService dbService;

    @Override
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies";

        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieid(rs.getString("movieid"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setSynopsis(rs.getString("synopsis"));
                movie.setLength(rs.getInt("length"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            // This triggers the Exception Flow 1.1 in your UC-5 document
            System.err.println("Exception Flow: Movie Listings not fetched from database.");
            e.printStackTrace();
        }
        return movies;
    }

    @Override
    public List<Movie> searchMoviesByName(String name) {
        List<Movie> results = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title ILIKE ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieid(rs.getString("movieid"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                results.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Database error during search: " + e.getMessage());
        }
        return results;
    }

    @Override
    public Movie getMovieById(String movieId) {
        String sql = "SELECT * FROM movies WHERE movieid = ?";
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieid(rs.getString("movieid"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setSynopsis(rs.getString("synopsis"));
                movie.setLength(rs.getInt("length"));
                return movie;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching movie details: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Movie> getMoviesByGenre(String genre) {
        List<Movie> filteredMovies = new ArrayList<>();
        // Exact match for the genre column
        String sql = "SELECT * FROM movies WHERE genre = ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, genre);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieid(rs.getString("movieid"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setSynopsis(rs.getString("synopsis"));
                movie.setLength(rs.getInt("length"));
                filteredMovies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Genre Filtering Failed: " + e.getMessage());
        }
        return filteredMovies;
    }

    @Override
    public List<Session> getSessionsByMovie(String movieId) {
        List<Session> sessions = new ArrayList<>();
        // Use * to ensure we get total_seats and available_seats
        String sql = "SELECT * FROM sessions WHERE movieid = ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Use your helper method here to get ALL fields including seats
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }   

    @Override
    public Session getSessionById(String sessionId) {
        String sql = "SELECT * FROM sessions WHERE sessionid = ?";
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Use your helper method here too
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error selecting session: " + e.getMessage());
        }
        return null;
    }

    // Your helper method is perfect - it captures the dynamic seat data!
    private Session mapResultSetToSession(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionid(rs.getString("sessionid"));
        s.setMovieid(rs.getString("movieid")); // Don't forget the movieid link
        s.setShowDate(rs.getDate("show_date").toString());
        s.setShowTime(rs.getTime("show_time").toString());
        s.setHallName(rs.getString("hall_name"));
        s.setLanguage(rs.getString("language"));
        
        // Fetch seat data for UC-12
        s.setTotalSeats(rs.getInt("total_seats"));
        s.setAvailableSeats(rs.getInt("available_seats"));
        return s;
    }

    @Override
    public List<Seat> getSeatLayout(String sessionId) {
        List<Seat> layout = new ArrayList<>();
        String sql = "SELECT seat_number, seat_type, price, status FROM seats WHERE session_id = ? ORDER BY seat_number ASC";
        
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Seat s = new Seat();
                s.setSeatNumber(rs.getInt("seat_number"));
                s.setType(rs.getString("seat_type"));
                s.setPrice(rs.getDouble("price"));
                s.setStatus(rs.getString("status"));
                layout.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seat layout: " + e.getMessage());
        }
        return layout;
    }

    @Override
    public List<Integer> getRecommendedSeats(int numSeats, String sessionId) {
        int[] ROW_PREFERENCE = {4, 3, 5, 2, 6, 1, 7, 0};
        int COLUMNS = 8;

        // 1. Get current occupied seats for this session
        List<Integer> occupiedSeats = new ArrayList<>();
        String sql = "SELECT seat_number FROM seats WHERE session_id = ? AND status = 'BOOKED'";
        
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

            // Check every possible starting position in the row for 'numSeats'
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
                    // Calculate Center Offset (same as your JS getCenterOffset)
                    double selectionCenter = i + (numSeats - 1) / 2.0;
                    double theaterCenter = (COLUMNS - 1) / 2.0;
                    double offset = Math.abs(selectionCenter - theaterCenter);

                    if (offset < minOffset) {
                        minOffset = offset;
                        bestSeatsInRow = potentialSlice;
                    }
                }
            }

            // If we found a valid block in the preferred row, return it immediately
            if (bestSeatsInRow != null) {
                return bestSeatsInRow;
            }
        }
        return null; 

    }

    @Override
    public boolean lockSeats(String sessionId, List<Integer> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) return false;

        // Build a query like: UPDATE seats ... WHERE seat_number IN (1, 2, 3)
        StringBuilder sql = new StringBuilder("UPDATE seats SET status = 'LOCKED' WHERE session_id = ? AND status = 'AVAILABLE' AND seat_number IN (");
        for (int i = 0; i < seatNumbers.size(); i++) {
            sql.append("?");
            if (i < seatNumbers.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, sessionId);
            for (int i = 0; i < seatNumbers.size(); i++) {
                pstmt.setInt(i + 2, seatNumbers.get(i));
            }
            
            int rowsUpdated = pstmt.executeUpdate();
            
            // Success only if we locked the exact amount requested
            return rowsUpdated == seatNumbers.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

   @Override
    public boolean releaseSeats(String sessionId, List<Integer> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) return false;

        // Build the dynamic IN clause: (?, ?, ?)
        StringBuilder sql = new StringBuilder("UPDATE seats SET status = 'AVAILABLE' ")
                .append("WHERE session_id = ? AND status = 'LOCKED' AND seat_number IN (");
        
        for (int i = 0; i < seatNumbers.size(); i++) {
            sql.append("?");
            if (i < seatNumbers.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set the sessionId first
            pstmt.setString(1, sessionId);
            
            // Set each seat number in the IN clause starting from parameter index 2
            for (int i = 0; i < seatNumbers.size(); i++) {
                pstmt.setInt(i + 2, seatNumbers.get(i));
            }
            
            int rowsUpdated = pstmt.executeUpdate();
            
            // Returns true if at least one seat was successfully released
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error releasing seats: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getRecommendations(String userId) {
        List<String> recommendedIds = new ArrayList<>();

        // We use exactly your working query, just replacing the IDs with ?::uuid
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
    public void displayMovieDetails(String movieId) {
        String sql = "SELECT * FROM movies WHERE movieid = ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n========================================");
                System.out.println("         MOVIE DETAILS          ");
                System.out.println("========================================");
                System.out.println("TITLE    : " + rs.getString("title"));
                System.out.println("GENRE    : " + rs.getString("genre"));
                System.out.println("DURATION : " + rs.getInt("length") + " mins");
                System.out.println("SYNOPSIS : " + rs.getString("synopsis"));
                System.out.println("----------------------------------------");
                System.out.println("Action   : Use 'cinema:sessions " + movieId + "' to book now!");
                System.out.println("========================================\n");
            } else {
                System.out.println("[!] Error: Movie ID " + movieId + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
