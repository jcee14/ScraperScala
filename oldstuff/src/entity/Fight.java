package entity;

import com.mysql.jdbc.Connection;
import db.DBConnFactory;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by justin on 7/14/2014.
 */
public class Fight {
    private int id;
    private String f1Id;
    private String f2Id;
    private String winnerId;
    private String method;
    private String referee;
    private Integer round;
    private Integer time;
    private int eventId;
    private boolean inSync;

    public Fight(String f1Id, String f2Id, String winnerId, String method, String referee, Integer round, Integer time, int eventId) {
        this.f1Id = f1Id;
        this.f2Id = f2Id;
        this.winnerId = winnerId;
        this.method = method;
        this.referee = referee;
        this.round = round;
        this.time = time;
        this.eventId = eventId;
        inSync = false;
    }
    public Fight() {
        inSync = false;
    }

    public int getId() {
        return id;
    }

    public boolean isInSync() { return inSync; }

    @Override
    public String toString() {
        return f1Id + ", " + f2Id + ", " + eventId;
    }

    public int write() {
        if(inSync) {
            throw new RuntimeException("trying to write already synced fight " + id);
        }
        Connection conn = DBConnFactory.getInstance();
        try {
            PreparedStatement s = conn.prepareStatement("insert into fights (f1_id, f2_id, winner_id, method, referee, round, time, event_id) values (?, ?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            s.setString(1, f1Id);
            s.setString(2, f2Id);
            s.setString(3, winnerId);
            s.setString(4, method);
            s.setString(5, referee);
            s.setInt(6, round);
            s.setInt(7, time != null ? time : 0);
            s.setInt(8, eventId);
            s.execute();

            ResultSet rs = s.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            inSync = true;
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
