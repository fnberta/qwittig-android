/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.presentation.common.viewmodels.CardTopProgressViewModel;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides an implementation of the {@link TaskDetailsItemModel} interface for a task item.
 */
public class TasksItem extends BaseObservable implements TasksItemModel, CardTopProgressViewModel {

    private boolean mCurrentUserResponsible;
    private ViewListener mView;
    private Task mTask;

    public TasksItem(@NonNull Task task, @NonNull Identity currentIdentity) {
        mCurrentUserResponsible = Objects.equals(currentIdentity.getObjectId(), task.getIdentityResponsible().getObjectId());
        mTask = task;
    }

    public void setView(@NonNull ViewListener view) {
        mView = view;
    }

    public Task getTask() {
        return mTask;
    }

    @Bindable
    public String getTaskTitle() {
        return mTask.getTitle();
    }

    @Bindable
    @StringRes
    public int getTaskTimeFrame() {
        final String timeFrame = mTask.getTimeFrame();
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
    public String getTaskUserResponsibleNickname() {
        return mTask.getIdentityResponsible().getNickname();
    }

    @Bindable
    public String getTaskUserResponsibleAvatar() {
        return mTask.getIdentityResponsible().getAvatarUrl();
    }

    @Bindable
    public String getTaskIdentities() {
        return mView.buildTaskIdentitiesString(mTask.getIdentities());
    }

    @Bindable
    public String getTaskDeadline() {
        final int daysToDeadline = getDaysToTaskDeadline();
        if (daysToDeadline == 0) {
            return mView.buildTaskDeadlineString(R.string.deadline_today);
        } else if (daysToDeadline == -1) {
            return mView.buildTaskDeadlineString(R.string.yesterday);
        } else if (daysToDeadline < 0) {
            return mView.buildTaskDeadlineString(R.string.deadline_text_neg, daysToDeadline * -1);
        } else {
            return mView.buildTaskDeadlineString(R.string.deadline_text_pos, daysToDeadline);
        }
    }

    @Bindable
    public boolean isTaskDeadlinePast() {
        final int daysToDeadline = getDaysToTaskDeadline();
        return daysToDeadline != 0 && (daysToDeadline == -1 || daysToDeadline < 0);
    }

    /**
     * Calculates the days it takes from today until the deadline of the task is reached.
     *
     * @return the number of days until the deadline is reached
     */
    private int getDaysToTaskDeadline() {
        final Date deadlineDate = mTask.getDeadline();

        final Calendar today = DateUtils.getCalendarInstanceUTC();
        final Calendar deadline = DateUtils.getCalendarInstanceUTC();
        deadline.setTime(deadlineDate);

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
        return mTask.isLoading();
    }

    @Bindable
    public boolean isCurrentUserResponsible() {
        return mCurrentUserResponsible;
    }

    @Bindable
    @StringRes
    public int getTaskDoneText() {
        final String timeFrame = mTask.getTimeFrame();
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
    public int getType() {
        return Type.TASK;
    }

    public interface ViewListener {
        String buildTaskIdentitiesString(@NonNull List<Identity> identities);

        String buildTaskDeadlineString(@StringRes int deadline, Object... args);
    }
}
