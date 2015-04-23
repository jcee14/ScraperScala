package db;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by justin on 7/14/2014.
 */
public class DBConnFactory {
    private static Connection _conn = null;
    private DBConnFactory() {}

    public static Connection getInstance() {

        if(_conn == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                _conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/fightdb?" +
                "user=fightwriter&password=asdf");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return _conn;
    }
}
