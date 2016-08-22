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

    private final String title;
    private final String timeFrame;
    private final String nickname;
    private final String avatar;
    private final List<Identity> identities;
    private final Date deadline;
    private final boolean currentUserResponsible;
    private boolean itemLoading;
    private ViewListener view;

    public TasksItem(@EventType int eventType,
                     @NonNull Task task,
                     @NonNull Identity identity,
                     @NonNull List<Identity> identities,
                     @NonNull String currentIdentityId,
                     boolean itemLoading) {
        super(eventType, task.getId());

        title = task.getTitle();
        timeFrame = task.getTimeFrame();
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        this.identities = identities;
        deadline = task.getDeadline();
        currentUserResponsible = Objects.equals(identity.getId(), currentIdentityId);
        this.itemLoading = itemLoading;
    }

    public void setView(@NonNull ViewListener view) {
        this.view = view;
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

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public List<Identity> getIdentities() {
        return identities;
    }

    @Bindable
    public String getIdentitiesText() {
        return view.buildIdentitiesString(identities);
    }

    public Date getDeadline() {
        return deadline;
    }

    @Bindable
    public String getDeadlineText() {
        final int daysToDeadline = getDaysToDeadline();
        if (daysToDeadline == 0) {
            return view.buildDeadlineString(R.string.deadline_today);
        } else if (daysToDeadline == -1) {
            return view.buildDeadlineString(R.string.yesterday);
        } else if (daysToDeadline < 0) {
            return view.buildDeadlineString(R.string.deadline_text_neg, daysToDeadline * -1);
        } else {
            return view.buildDeadlineString(R.string.deadline_text_pos, daysToDeadline);
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
        deadline.setTime(this.deadline);

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
        return itemLoading;
    }

    public void setItemLoading(boolean itemLoading) {
        this.itemLoading = itemLoading;
    }

    @Bindable
    public boolean isCurrentUserResponsible() {
        return currentUserResponsible;
    }

    @Bindable
    @StringRes
    public int getDoneText() {
        switch (timeFrame) {
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
        return deadline.compareTo(tasksItem.getDeadline());
    }

    public interface ViewListener {
        String buildIdentitiesString(@NonNull List<Identity> identities);

        String buildDeadlineString(@StringRes int deadline, Object... args);
    }
}
