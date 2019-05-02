package ILocal.security;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

public class JwtUser {

    private String userName;
    private long id;
    private Timestamp date;

    public JwtUser(long id, String userName,long date) {
        this.id = id;
        this.userName = userName;
        this.date = new Timestamp(date);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public long getId() {
        return id;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
