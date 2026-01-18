package com.cbse.cinema.api.service;

import com.cbse.cinema.api.model.User;
import java.util.List;

public interface UserService {
    void registerUser(User user);

    User findByEmail(String email);

    User findById(String id);

    void updateUser(User user);

    void updateGenres(String userId, List<String> genres);

    void addFavorite(String userId, String movieId);
}