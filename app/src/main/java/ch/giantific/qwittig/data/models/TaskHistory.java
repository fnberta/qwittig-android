package ch.giantific.qwittig.data.models;

import com.parse.ParseUser;

import java.util.Date;

import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 13.09.15.
 */
public class TaskHistory {

    private User mUser;
    private Date mDate;

    public User getUser() {
        return mUser;
    }

    public void setUser(ParseUser user) {
        mUser = (User) user;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public TaskHistory(ParseUser user, Date date) {
        mUser = (User) user;
        mDate = date;
    }
}
