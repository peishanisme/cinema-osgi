package com.cbse.cinema.db;

import com.cbse.cinema.api.service.DatabaseService; 
import org.osgi.service.component.annotations.Component; 
import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException;

@Component(service = DatabaseService.class)
public class SupabaseServiceImpl implements DatabaseService {
    private final String url = "jdbc:postgresql://db.lnuazfaxqcdykxsartsd.supabase.co:5432/postgres";
    private final String user = "postgres";
    private final String password = "cbse3006cinema-osgi";

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
    

