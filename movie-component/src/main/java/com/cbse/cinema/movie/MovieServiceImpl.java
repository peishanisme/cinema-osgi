package com.cbse.cinema.movie;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;
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
        String sql = "SELECT * FROM sessions WHERE movieid = ?";

        try (Connection conn = dbService.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Session s = new Session();
                s.setSessionid(rs.getString("sessionid"));
                s.setShowDate(rs.getDate("show_date").toString());
                s.setShowTime(rs.getTime("show_time").toString());
                s.setHallName(rs.getString("hall_name"));
                s.setLanguage(rs.getString("language")); 
                sessions.add(s);
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
                Session s = new Session();
                s.setSessionid(rs.getString("sessionid"));
                s.setMovieid(rs.getString("movieid"));
                s.setShowDate(rs.getDate("show_date").toString());
                s.setShowTime(rs.getTime("show_time").toString());
                s.setHallName(rs.getString("hall_name"));
                s.setLanguage(rs.getString("language"));
                return s;
            }
        } catch (SQLException e) {
            System.err.println("Error selecting session: " + e.getMessage());
        }
        return null;
    }
}