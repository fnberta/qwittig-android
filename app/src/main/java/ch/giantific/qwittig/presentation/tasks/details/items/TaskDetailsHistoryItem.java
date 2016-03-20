/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a history entry of a {@link Task} including a {@link User} and the {@link Date}
 * he/she completed the task.
 */
public class TaskDetailsHistoryItem extends BaseObservable
        implements TaskDetailsBaseItem, Comparable<TaskDetailsHistoryItem> {

    private final String mTaskEventDate;
    private final Identity mTaskEventIdentity;

    public TaskDetailsHistoryItem(@NonNull TaskHistoryEvent taskHistoryEvent) {
        mTaskEventIdentity = taskHistoryEvent.getIdentity();
        final DateFormat dateFormatter = DateUtils.getDateFormatter(false);
        mTaskEventDate = dateFormatter.format(taskHistoryEvent.getDate());
    }

    @Bindable
    public String getTaskEventIdentityNickname() {
        return mTaskEventIdentity.getNickname();
    }

    @Bindable
    public String getTaskEventIdentityAvatar() {
        return mTaskEventIdentity.getAvatarUrl();
    }

    @Bindable
    public String getTaskEventDate() {
        return mTaskEventDate;
    }

    @Override
    public int getType() {
        return Type.HISTORY;
    }

    @Override
    public int compareTo(@NonNull TaskDetailsHistoryItem another) {
        return this.getTaskEventDate().compareTo(another.getTaskEventDate());
    }
}
