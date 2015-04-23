package entity;

import db.DBConnFactory;
import entity.Event;
import org.junit.After;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by justin on 7/15/2014.
 */
public class TestEvent {
    @Test
    public void testEvent() throws SQLException {
        Event e = new Event("UFC 1", "somewher", 1, "1990-01-01", "/events/");
        e.write();
        Connection conn = DBConnFactory.getInstance();
        Statement statement = null;
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select id, name, location, organization, date from events");
        rs.next();
        assert(rs.getInt(1) > 0);
        assert(rs.getString(2).length() > 0);
        assert(rs.getString(3).length() > 0);
        assert(rs.getString(4).length() > 0);
        assert(rs.getDate(5).after(Date.valueOf("1900-01-01")));
        deleteTestEntry();
    }

    public void deleteTestEntry() throws SQLException {
        Connection conn = DBConnFactory.getInstance();
        Statement statement = null;
        statement = conn.createStatement();
        int upCount = statement.executeUpdate("delete from events where location='somewher'");
        assertEquals(upCount, 1);
    }
}
