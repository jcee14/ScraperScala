package entity;

import com.mysql.jdbc.Connection;
import db.DBConnFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin on 7/14/2014.
 */
public class Fighter {
    private String name;
    private Date birthday;
    private String url;
    private boolean inSync;

    public Fighter(String name, String url) {
        this.name = name;
        this.url = url;
        inSync = false;
    }
    public Fighter() {
        inSync = false;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return name;
    }

    public void write() {
        if(inSync) {
            throw new RuntimeException("trying to write already synced fighter " + name);
        }
        Connection conn = DBConnFactory.getInstance();
        try {
            PreparedStatement s = conn.prepareStatement("insert into fighters (name, birthday, url) values (?, ?, ?)");
            s.setString(1, name);
            s.setDate(2, birthday);
            s.setString(3, url);
            s.execute();
            inSync = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInSync() { return inSync; }

    public Fighter fetchBirthday() {

        return this;
    }

    public boolean equals(Fighter f) {
        return name.equals(f.name);
    }

    public static List<Fighter> getAll() {
        List<Fighter> fighterList = new ArrayList<>();
        Connection conn = DBConnFactory.getInstance();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("select name, birthday, url from fighters");
            while(rs.next()) {
                Fighter fighter = new Fighter();
                fighter.name = rs.getString(1);
                fighter.birthday = rs.getDate(2);
                fighter.url = rs.getString(3);
                fighter.inSync = true;
                fighterList.add(fighter);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return fighterList;
    }
}
