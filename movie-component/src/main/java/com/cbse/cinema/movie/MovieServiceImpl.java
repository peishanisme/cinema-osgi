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
        
        String sql = "SELECT s.*, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid) as calculated_total, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid AND status = 'AVAILABLE') as calculated_available " +
                    "FROM sessions s " +
                    "WHERE s.movieid = ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sessions for movie: " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    @Override
    public Session getSessionById(String sessionId) {

        String sql = "SELECT s.*, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid) as calculated_total, " +
                    "(SELECT COUNT(*) FROM seats WHERE session_id = s.sessionid AND status = 'AVAILABLE') as calculated_available " +
                    "FROM sessions s WHERE s.sessionid = ?";
                    
        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error selecting session: " + e.getMessage());
        }
        return null;
    }

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
    public boolean lockSeats(String userId, String sessionId, List<Integer> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) return false;

        StringBuilder sql = new StringBuilder("UPDATE seats SET status = 'LOCKED', locked_by = CAST(? AS UUID), locked_at = CURRENT_TIMESTAMP ")
                .append("WHERE session_id = ? AND status = 'AVAILABLE' AND seat_number IN (");
        
        for (int i = 0; i < seatNumbers.size(); i++) {
            sql.append("?");
            if (i < seatNumbers.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, userId);    
            pstmt.setString(2, sessionId); 
            
            for (int i = 0; i < seatNumbers.size(); i++) {
                pstmt.setInt(i + 3, seatNumbers.get(i)); 
            }
            
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated == seatNumbers.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

   @Override
    public boolean releaseSeats(String userId, String sessionId, List<Integer> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) return false;

        // Use StringBuilder to allow .append()
        StringBuilder sql = new StringBuilder("UPDATE seats SET status = 'AVAILABLE', locked_by = NULL, locked_at = NULL ")
                .append("WHERE session_id = CAST(? AS UUID) AND locked_by = CAST(? AS UUID) AND seat_number IN (");

        for (int i = 0; i < seatNumbers.size(); i++) {
            sql.append("?");
            if (i < seatNumbers.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Parameter 1: sessionId
            pstmt.setString(1, sessionId);
            
            // Parameter 2: userId
            pstmt.setString(2, userId);
            
            // Parameters 3 onwards: the seat numbers
            for (int i = 0; i < seatNumbers.size(); i++) {
                pstmt.setInt(i + 3, seatNumbers.get(i)); 
            }
            
            int rowsUpdated = pstmt.executeUpdate();
            
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error releasing seats: " + e.getMessage());
            return false;
        }
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
