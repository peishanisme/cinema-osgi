package com.cbse.cinema.api.service;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;

import java.util.List;

public interface MovieService {
    List<Movie> getAllMovies();

    List<Movie> searchMoviesByName(String name);

    Movie getMovieById(String movieId);

    List<Movie> getMoviesByGenre(String genre);

    List<Session> getSessionsByMovie(String movieId);

    Session getSessionById(String sessionId);
}
