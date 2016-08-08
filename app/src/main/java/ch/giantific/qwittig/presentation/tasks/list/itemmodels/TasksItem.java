/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import ch.giantific.qwittig.presentation.common.itemmodels.CardTopProgressItemModel;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides an implementation of the {@link TaskDetailsItemModel} interface for a task item.
 */
public class TasksItem extends BaseChildItemModel
        implements TasksItemModel, CardTopProgressItemModel, Comparable<TasksItem> {

    private final String mTitle;
    private final String mTimeFrame;
    private final String mNickname;
    private final String mAvatar;
    private final List<Identity> mIdentities;
    private final Date mDeadline;
    private final boolean mCurrentUserResponsible;
    private boolean mItemLoading;
    private ViewListener mView;

    public TasksItem(@EventType int eventType,
                     @NonNull Task task,
                     @NonNull Identity identity,
                     @NonNull List<Identity> identities,
                     @NonNull String currentIdentityId,
                     boolean itemLoading) {
        super(eventType, task.getId());

        mTitle = task.getTitle();
        mTimeFrame = task.getTimeFrame();
        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
        mIdentities = identities;
        mDeadline = task.getDeadline();
        mCurrentUserResponsible = Objects.equals(identity.getId(), currentIdentityId);
        mItemLoading = itemLoading;
    }

    public void setView(@NonNull ViewListener view) {
        mView = view;
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    public String getTimeFrame() {
        return mTimeFrame;
    }

    @Bindable
    @StringRes
    public int getTimeFrameText() {
        switch (mTimeFrame) {
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
                throw new RuntimeException("Unsupported time frame " + mTimeFrame);
        }
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    public List<Identity> getIdentities() {
        return mIdentities;
    }

    @Bindable
    public String getIdentitiesText() {
        return mView.buildIdentitiesString(mIdentities);
    }

    public Date getDeadline() {
        return mDeadline;
    }

    @Bindable
    public String getDeadlineText() {
        final int daysToDeadline = getDaysToDeadline();
        if (daysToDeadline == 0) {
            return mView.buildDeadlineString(R.string.deadline_today);
        } else if (daysToDeadline == -1) {
            return mView.buildDeadlineString(R.string.yesterday);
        } else if (daysToDeadline < 0) {
            return mView.buildDeadlineString(R.string.deadline_text_neg, daysToDeadline * -1);
        } else {
            return mView.buildDeadlineString(R.string.deadline_text_pos, daysToDeadline);
        }
    }

    @Bindable
    public boolean isDeadlinePast() {
        final int daysToDeadline = getDaysToDeadline();
        return daysToDeadline != 0 && (daysToDeadline == -1 || daysToDeadline < 0);
    }

    /**
     * Calculates the days it takes from today until the deadline of the task is reached.
     *
     * @return the number of days until the deadline is reached
     */
    private int getDaysToDeadline() {
        final Calendar today = DateUtils.getCalendarInstanceUTC();
        final Calendar deadline = DateUtils.getCalendarInstanceUTC();
        deadline.setTime(mDeadline);

        if (today.get(Calendar.YEAR) == deadline.get(Calendar.YEAR)) {
            return deadline.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
        }

        int extraDays = 0;
        if (deadline.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
            while (deadline.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
                deadline.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += deadline.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - today.get(Calendar.DAY_OF_YEAR) + deadline.get(Calendar.DAY_OF_YEAR);
        }
        if (deadline.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
            while (deadline.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
                deadline.add(Calendar.YEAR, 1);
                // getActualMaximum() important for leap years
                extraDays += deadline.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return (extraDays - deadline.get(Calendar.DAY_OF_YEAR) + today.get(Calendar.DAY_OF_YEAR)) * -1;
        }

        return 0;
    }

    @Override
    @Bindable
    public boolean isItemLoading() {
        return mItemLoading;
    }

    public void setItemLoading(boolean itemLoading) {
        mItemLoading = itemLoading;
    }

    @Bindable
    public boolean isCurrentUserResponsible() {
        return mCurrentUserResponsible;
    }

    @Bindable
    @StringRes
    public int getDoneText() {
        switch (mTimeFrame) {
            case TimeFrame.ONE_TIME:
                return R.string.task_done_single;
            case TimeFrame.AS_NEEDED:
                return R.string.task_done_single;
            case TimeFrame.DAILY:
                return R.string.task_done_today;
            case TimeFrame.WEEKLY:
                return R.string.task_done_week;
            case TimeFrame.MONTHLY:
                return R.string.task_done_month;
            case TimeFrame.YEARLY:
                return R.string.task_done_year;
            default:
                return R.string.task_done_single;
        }
    }

    @Override
    public int getViewType() {
        return Type.TASK;
    }

    @Override
    public int compareTo(@NonNull TasksItem tasksItem) {
        return mDeadline.compareTo(tasksItem.getDeadline());
    }

    public interface ViewListener {
        String buildIdentitiesString(@NonNull List<Identity> identities);

        String buildDeadlineString(@StringRes int deadline, Object... args);
    }
}
