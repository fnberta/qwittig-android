/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a history entry of a task including a user and the date
 * he/she completed the task.
 */
public class TaskDetailsHistoryItem extends BaseChildItemModel
        implements TaskDetailsItemModel, Comparable<TaskDetailsHistoryItem> {

    private final String mEventDate;
    private String mNickname;
    private String mAvatar;

    public TaskDetailsHistoryItem(@EventType int eventType,
                                  @NonNull TaskHistoryEvent taskHistoryEvent,
                                  @NonNull Identity identity) {
        super(eventType, taskHistoryEvent.getId());

        final DateFormat dateFormatter = DateUtils.getDateFormatter(false);
        mEventDate = dateFormatter.format(taskHistoryEvent.getDate());
        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Bindable
    public String getEventDate() {
        return mEventDate;
    }

    @Override
    public int getViewType() {
        return Type.HISTORY;
    }

    @Override
    public int compareTo(@NonNull TaskDetailsHistoryItem another) {
        return this.getEventDate().compareTo(another.getEventDate());
    }
}
