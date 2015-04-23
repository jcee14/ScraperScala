package entity;

import db.DBConnFactory;
import entity.Event;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by justin on 7/15/2014.
 */
public class TestOrganization {
    String testOrgName = "Radiance";
    @Test
    public void testEvent() throws SQLException {
        Organization org = new Organization(0, testOrgName);
        org.write();
        try {
            Connection conn = DBConnFactory.getInstance();
            Statement statement = null;
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select id, name from organizations where id=0");
            rs.next();
            assert (rs.getInt(1) == 0);
            assert (rs.getString(2).equals(testOrgName));
        }
        finally {
            deleteTestEntry();
        }
    }

    public void deleteTestEntry() throws SQLException {
        Connection conn = DBConnFactory.getInstance();
        Statement statement = null;
        statement = conn.createStatement();
        int upCount = statement.executeUpdate("delete from organizations where id=0");
        assert (upCount == 1);
    }
}
