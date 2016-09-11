/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

import ch.giantific.qwittig.domain.models.AssignmentHistory;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a history entry of a task including a user and the date
 * he/she completed the task.
 */
public class AssignmentDetailsHistoryItem extends BaseObservable implements AssignmentDetailsItemModel,
        Comparable<AssignmentDetailsHistoryItem> {

    private final Date eventDate;
    private final String eventDateText;
    private String nickname;
    private String avatar;

    public AssignmentDetailsHistoryItem(@NonNull AssignmentHistory assignmentHistory,
                                        @NonNull Identity identity) {
        final DateFormat dateFormatter = DateUtils.getDateFormatter(false);
        eventDate = assignmentHistory.getDateDate();
        eventDateText = dateFormatter.format(eventDate);
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

    public Date getEventDate() {
        return eventDate;
    }

    @Bindable
    public String getEventDateText() {
        return eventDateText;
    }

    @Override
    public int getViewType() {
        return Type.HISTORY;
    }

    @Override
    public int compareTo(@NonNull AssignmentDetailsHistoryItem another) {
        return eventDate.compareTo(another.getEventDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AssignmentDetailsHistoryItem that = (AssignmentDetailsHistoryItem) o;

        if (!eventDate.equals(that.getEventDate())) return false;
        if (!nickname.equals(that.getNickname())) return false;
        return avatar != null ? avatar.equals(that.getAvatar()) : that.getAvatar() == null;
    }

    @Override
    public int hashCode() {
        int result = eventDate.hashCode();
        result = 31 * result + nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        return result;
    }
}
