package db;

import db.DBConnFactory;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by justin on 7/14/2014.
 */
public class TestDbConn {
    @Test
    public void testDBConn() throws SQLException {
        Connection conn = DBConnFactory.getInstance();
        Statement statement = null;
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select 1");
        rs.next();
        assertEquals(1, rs.getInt(1));
    }
}
