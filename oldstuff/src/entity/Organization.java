package entity;

import com.mysql.jdbc.Connection;
import db.DBConnFactory;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin on 7/14/2014.
 */
public class Organization implements Comparable{
    private int _id;
    private String _name;
    private boolean _inSync = false;

    public Organization(int id, String name) {
        _id = id;
        _name = name;
    }

    public Organization(int id, String name, boolean inSync) {
        this(id, name);
        _inSync = inSync;
    }

    public int getId() {
        return _id;
    }

    public void write() {
        if(_inSync) {
            throw new RuntimeException(_name + " is already in the database!");
        }
        Connection conn = DBConnFactory.getInstance();
        try {
            PreparedStatement s = conn.prepareStatement("insert into organizations (id, name) values (?, ?)");
            s.setInt(1, _id);
            s.setString(2, _name);
            s.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Organization> getAll() {
        List<Organization> orgList = new ArrayList<>();
        Connection conn = DBConnFactory.getInstance();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("select id, name from organizations");
            while(rs.next()) {
                Organization org = new Organization(rs.getInt(1), rs.getString(2));
                orgList.add(org);
            }
        } catch (SQLException e) {
            System.err.println("error writing $id $name");
            throw new RuntimeException(e);
        }

        return orgList;
    }

    @Override
    public int compareTo(Object o) {
        return new Integer(_id).compareTo(new Integer(((Organization) o)._id));
    }
}
