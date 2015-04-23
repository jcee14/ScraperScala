package entity;

import db.DBConnFactory;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by justin on 7/15/2014.
 */
public class TestFighter {
    static String myUrl = "/fighters/Justin-The-Monsta-Choi-1234";
    @Test
    public void testEvent() throws SQLException {

        Fighter me = new Fighter("JCJC", myUrl);
        me.write();
        Connection conn = DBConnFactory.getInstance();
        /*Statement statement = null;
        statement = conn.createStatement();*/
        PreparedStatement stmt = conn.prepareStatement("select name, url from fighters where url=?");
        stmt.setString(1, myUrl);
        ResultSet rs = stmt.executeQuery();

        assert(rs.getString(1).equals("JCJC"));
        assert(rs.getDate(2).equals(myUrl));

        deleteTestEntry();
    }

    public void deleteTestEntry() throws SQLException {
        Connection conn = DBConnFactory.getInstance();
        PreparedStatement stmt = conn.prepareStatement("delete from fighters where url=?");
        stmt.setString(1, myUrl);
        int upCount = stmt.executeUpdate();
        assertEquals(upCount, 1);
    }
}
