/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.util.Date;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a history entry of a {@link Task} including a {@link User} and the {@link Date}
 * he/she completed the task.
 */
public class TaskHistoryItem extends BaseObservable
        implements DetailsItem, Comparable<TaskHistoryItem> {

    private final String mDate;
    private Identity mUser;

    public TaskHistoryItem(@NonNull Identity identity, @NonNull Date date) {
        mUser = identity;
        mDate = DateUtils.getDateFormatter(false).format(date);
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
    public String getDate() {
        return mDate;
    }

    @Override
    public int getType() {
        return Type.HISTORY;
    }

    @Override
    public int compareTo(@NonNull TaskHistoryItem another) {
        return this.getDate().compareTo(another.getDate());
    }
}
