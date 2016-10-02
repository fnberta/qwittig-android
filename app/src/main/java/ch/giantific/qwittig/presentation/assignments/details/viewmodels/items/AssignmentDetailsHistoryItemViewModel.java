/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.viewmodels.items;

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
public class AssignmentDetailsHistoryItemViewModel extends BaseObservable
        implements BaseAssignmentDetailsItemViewModel,
        Comparable<AssignmentDetailsHistoryItemViewModel> {

    private final Date eventDate;
    private final String eventDateText;
    private final String nickname;
    private final String avatar;

    public AssignmentDetailsHistoryItemViewModel(@NonNull AssignmentHistory assignmentHistory,
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
        return ViewType.HISTORY;
    }

    @Override
    public int compareTo(@NonNull AssignmentDetailsHistoryItemViewModel another) {
        return eventDate.compareTo(another.getEventDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AssignmentDetailsHistoryItemViewModel that = (AssignmentDetailsHistoryItemViewModel) o;

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
