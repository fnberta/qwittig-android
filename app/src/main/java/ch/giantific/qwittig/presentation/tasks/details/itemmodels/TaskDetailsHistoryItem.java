/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

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

    private final String eventDate;
    private String nickname;
    private String avatar;

    public TaskDetailsHistoryItem(@EventType int eventType,
                                  @NonNull TaskHistoryEvent taskHistoryEvent,
                                  @NonNull Identity identity) {
        super(eventType, taskHistoryEvent.getId());

        final DateFormat dateFormatter = DateUtils.getDateFormatter(false);
        eventDate = dateFormatter.format(taskHistoryEvent.getDate());
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Bindable
    public String getEventDate() {
        return eventDate;
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
