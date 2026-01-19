package com.cbse.cinema.api.service;

import java.util.List;

import com.cbse.cinema.api.model.Movie;
import com.cbse.cinema.api.model.Session;

public interface RecommendationService {
    List<Integer> getRecommendedSeats(int numSeats, String sessionId);

    List<String> getRecommendations(String userId);

    List<Movie> getRecommendedMovieDetails(String userId);

    List<Session> getRecommendedSessions(String movieId);
}
