/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;

import ch.giantific.qwittig.domain.models.AssignmentHistoryEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a history entry of a task including a user and the date
 * he/she completed the task.
 */
public class AssignmentDetailsHistoryItem extends BaseObservable implements AssignmentDetailsItemModel,
        Comparable<AssignmentDetailsHistoryItem> {

    private final String eventDate;
    private String nickname;
    private String avatar;

    public AssignmentDetailsHistoryItem(@NonNull AssignmentHistoryEvent assignmentHistoryEvent,
                                        @NonNull Identity identity) {
        final DateFormat dateFormatter = DateUtils.getDateFormatter(false);
        eventDate = dateFormatter.format(assignmentHistoryEvent.getDate());
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
    public int compareTo(@NonNull AssignmentDetailsHistoryItem another) {
        return this.getEventDate().compareTo(another.getEventDate());
    }
}
