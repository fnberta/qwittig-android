/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.parse.ParseUser;

import java.util.Date;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Represents a history entry of a {@link Task} including a {@link User} and the {@link Date}
 * he/she completed the task.
 */
public class TaskHistory extends BaseObservable
        implements Comparable<TaskHistory> {

    private User mUser;
    private Date mDate;

    public User getUser() {
        return mUser;
    }

    public void setUser(@NonNull ParseUser user) {
        mUser = (User) user;
    }

    @Bindable
    public String getUserNickname() {
        return mUser.getNickname();
    }

    @Bindable
    public byte[] getUserAvatar() {
        return mUser.getAvatar();
    }

    @Bindable
    public Date getDate() {
        return mDate;
    }

    public void setDate(@NonNull Date date) {
        mDate = date;
    }

    public TaskHistory(@NonNull ParseUser user, @NonNull Date date) {
        mUser = (User) user;
        mDate = date;
    }

    @Override
    public int compareTo(@NonNull TaskHistory another) {
        return this.getDate().compareTo(another.getDate());
    }
}
