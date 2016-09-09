/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Date;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentDetailsItemModel} interface for a task item.
 */
public class AssignmentItem extends BaseChildItemModel
        implements AssignmentItemModel, Comparable<AssignmentItem> {

    private final String title;
    private final String timeFrame;
    private final Date deadline;
    private final String deadlineText;
    private final int daysToDeadline;
    private final String[] identitiesSorted;
    private final String nickname;
    private final String avatar;
    private final boolean pending;
    private final String upNext;
    private final boolean responsible;

    public AssignmentItem(@EventType int eventType,
                          @NonNull Assignment assignment,
                          @NonNull Identity identityResponsible,
                          @NonNull String deadlineText,
                          @NonNull String upNext,
                          @NonNull String currentIdentityId) {
        super(eventType, assignment.getId());

        title = assignment.getTitle();
        timeFrame = assignment.getTimeFrame();
        deadline = assignment.getDeadlineDate();
        daysToDeadline = assignment.getDaysToDeadline();
        identitiesSorted = assignment.getIdentityIdsSorted();

        nickname = identityResponsible.getNickname();
        avatar = identityResponsible.getAvatar();
        pending = identityResponsible.isPending();

        this.deadlineText = deadlineText;
        this.upNext = upNext;
        this.responsible = Objects.equals(identityResponsible.getId(), currentIdentityId);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    @Bindable
    @StringRes
    public int getTimeFrameText() {
        switch (timeFrame) {
            case TimeFrame.ONE_TIME:
                return R.string.time_frame_one_time;
            case TimeFrame.AS_NEEDED:
                return R.string.time_frame_as_needed;
            case TimeFrame.DAILY:
                return R.string.time_frame_daily;
            case TimeFrame.WEEKLY:
                return R.string.time_frame_weekly;
            case TimeFrame.MONTHLY:
                return R.string.time_frame_monthly;
            case TimeFrame.YEARLY:
                return R.string.time_frame_yearly;
            default:
                throw new RuntimeException("Unsupported time frame " + timeFrame);
        }
    }

    public Date getDeadline() {
        return deadline;
    }

    @Bindable
    public String getDeadlineText() {
        return deadlineText;
    }

    @Bindable
    @ColorRes
    public int getDeadlineColor() {
        if (Objects.equals(timeFrame, TimeFrame.AS_NEEDED)) {
            return android.R.color.secondary_text_light;
        }

        return daysToDeadline != 0 && (daysToDeadline == -1 || daysToDeadline < 0)
                ? R.color.red
                : R.color.green;
    }

    public String[] getIdentitiesSorted() {
        return identitiesSorted;
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public boolean isPending() {
        return pending;
    }

    @Bindable
    public String getUpNext() {
        return upNext;
    }

    @Bindable
    public boolean isResponsible() {
        return responsible;
    }

    @Bindable
    @StringRes
    public int getDoneText() {
        switch (timeFrame) {
            case TimeFrame.ONE_TIME:
                return R.string.assignment_done_single;
            case TimeFrame.AS_NEEDED:
                return R.string.assignment_done_single;
            case TimeFrame.DAILY:
                return R.string.assignment_done_today;
            case TimeFrame.WEEKLY:
                return R.string.assignment_done_week;
            case TimeFrame.MONTHLY:
                return R.string.assignment_done_month;
            case TimeFrame.YEARLY:
                return R.string.assignment_done_year;
            default:
                return R.string.assignment_done_single;
        }
    }

    @Override
    public int getViewType() {
        return Type.ASSIGNMENT;
    }

    @Override
    public int compareTo(@NonNull AssignmentItem itemModel) {
        if ((responsible && itemModel.isResponsible()) ||
                !responsible && !itemModel.isResponsible()) {
            return deadline.compareTo(itemModel.getDeadline());
        }

        if (responsible && !itemModel.isResponsible()) {
            return -1;
        }

        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AssignmentItem that = (AssignmentItem) o;

        if (daysToDeadline != that.daysToDeadline) return false;
        if (pending != that.pending) return false;
        if (responsible != that.responsible) return false;
        if (!title.equals(that.title)) return false;
        if (!timeFrame.equals(that.timeFrame)) return false;
        if (!deadline.equals(that.deadline)) return false;
        if (!nickname.equals(that.nickname)) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        return upNext.equals(that.upNext);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + timeFrame.hashCode();
        result = 31 * result + deadline.hashCode();
        result = 31 * result + daysToDeadline;
        result = 31 * result + nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (pending ? 1 : 0);
        result = 31 * result + upNext.hashCode();
        result = 31 * result + (responsible ? 1 : 0);
        return result;
    }
}
