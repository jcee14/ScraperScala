package entity;

import db.DBConnFactory;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by justin on 7/15/2014.
 */
public class TestFight {
    @Test
    public void testEvent() throws SQLException {
        Fight f = new Fight("jc", "jb", "jc", "TKO", "Herb", 100, 1, 1);
        int id = f.write();
        Connection conn = DBConnFactory.getInstance();
        Statement statement = null;
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select id, f1_id, f2_id, winner_id, method, referee, time, event_id from fights where id="+id);
        rs.next();
        assert(rs.getInt(1) > 0);
        assert(rs.getString(2).length() > 0);
        assert(rs.getString(3).length() > 0);
        assert(rs.getString(4).length() > 0);
        assert(rs.getString(5).length() > 0);
        assert(rs.getString(6).length() > 0);
        assert(rs.getInt(7) > 0);
        assert(rs.getInt(8) > 0);

        statement = conn.createStatement();
        int upCount = statement.executeUpdate("delete from fights where id="+id);
        assertEquals(upCount, 1);
    }
}
