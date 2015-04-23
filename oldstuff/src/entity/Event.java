package entity;

import com.mysql.jdbc.Connection;
import db.DBConnFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin on 7/14/2014.
 */
public class Event {
    private int id;
    private String name;
    private String location;
    private String url;
    private int orgId;
    private Date date;
    private boolean inSync;

    public Event(String name, String location, int orgId, String date, String url) {
        this.name = name;
        this.location = location;
        this.orgId = orgId;
        this.date = Date.valueOf(date);
        this.url = url;
        inSync = false;
    }
    public Event() {
        inSync = false;
    }

    public int getId() {
        return id;
    }

    public String getUrl() { return url; }

    @Override
    public String toString() {
        return name + ", " + location + ", " + orgId + ", " + date + ", " + url;
    }

    public void write() {
        if(inSync) {
            throw new RuntimeException("trying to write already synced event " + name);
        }
        Connection conn = DBConnFactory.getInstance();
        try {
            PreparedStatement s = conn.prepareStatement("insert into events (name, location, org_id, date, url) values (?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            s.setString(1, name);
            s.setString(2, location);
            s.setInt(3, orgId);
            s.setDate(4, date);
            s.setString(5, url);
            s.execute();

            ResultSet rs = s.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            inSync = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static Event get(int eventId) {
        Connection conn = DBConnFactory.getInstance();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("select id, name, org_id, location, date, url from events where id="+eventId);
            rs.next();
            Event e = new Event();
            e.id = rs.getInt(1);
            e.name = rs.getString(2);
            e.orgId = rs.getInt(3);
            e.location = rs.getString(4);
            e.date = rs.getDate(5);
            e.url = rs.getString(6);
            e.inSync = true;
            return e;
        } catch (SQLException e) {
            System.err.println("error writing $id $name");
            throw new RuntimeException(e);
        }
    }

    public static List<Event> getAll() {
        List<Event> eventList = new ArrayList<>();
        Connection conn = DBConnFactory.getInstance();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("select id, name, org_id, location, date, url from events");
            while(rs.next()) {
                Event e = new Event();
                e.id = rs.getInt(1);
                e.name = rs.getString(2);
                e.orgId = rs.getInt(3);
                e.location = rs.getString(4);
                e.date = rs.getDate(5);
                e.url = rs.getString(6);
                e.inSync = true;
                eventList.add(e);
            }
        } catch (SQLException e) {
            System.err.println("error writing $id $name");
            throw new RuntimeException(e);
        }

        return eventList;
    }


}
