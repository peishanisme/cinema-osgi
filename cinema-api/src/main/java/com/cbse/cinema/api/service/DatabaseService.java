package com.cbse.cinema.api.service;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService {
    Connection getConnection() throws SQLException;
}