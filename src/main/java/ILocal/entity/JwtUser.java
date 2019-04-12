package ILocal.entity;

import java.sql.Date;
import java.util.Calendar;

public class JwtUser {

    private String userName;
    private long id;
    private Date date;

    public JwtUser(long id, String userName) {
        this.id = id;
        this.userName = userName;
        this.date=new Date(Calendar.getInstance().getTime().getTime());
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
