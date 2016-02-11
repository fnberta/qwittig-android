/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.util.Date;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Represents a history entry of a {@link Task} including a {@link User} and the {@link Date}
 * he/she completed the task.
 */
public class TaskHistory extends BaseObservable
        implements Comparable<TaskHistory> {

    private Identity mUser;
    private Date mDate;

    public TaskHistory(@NonNull Identity identity, @NonNull Date date) {
        mUser = identity;
        mDate = date;
    }

    public Identity getUser() {
        return mUser;
    }

    public void setUser(@NonNull Identity user) {
        mUser = user;
    }

    @Bindable
    public String getUserNickname() {
        return mUser.getNickname();
    }

    @Bindable
    public String getUserAvatar() {
        return mUser.getAvatarUrl();
    }

    @Bindable
    public Date getDate() {
        return mDate;
    }

    public void setDate(@NonNull Date date) {
        mDate = date;
    }

    @Override
    public int compareTo(@NonNull TaskHistory another) {
        return this.getDate().compareTo(another.getDate());
    }
}
