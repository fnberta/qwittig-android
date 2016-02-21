/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Calendar;
import java.util.Date;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.presentation.common.viewmodels.CardTopProgressViewModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskItem extends BaseObservable implements ListItem, CardTopProgressViewModel {

    private Task mTask;
    private Identity mTaskIdentityResponsible;
    private final Identity mCurrentIdentity;

    // TODO: fix users involved (nothing displayed right now)

    public TaskItem(@NonNull Task task, @NonNull Identity currentIdentity) {
        mCurrentIdentity = currentIdentity;
        setTaskInfo(task);
    }

    private void setTaskInfo(@NonNull Task task) {
        mTask = task;
        mTaskIdentityResponsible = task.getUserResponsible();
    }

    /**
     * Updates the task and the other info needed. Used to recycle view models.
     *
     * @param task the task to display
     */
    public void updateTaskInfo(@NonNull Task task) {
        setTaskInfo(task);
        notifyChange();
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
        return mTaskIdentityResponsible.getNickname();
    }

    @Bindable
    public String getTaskUserResponsibleAvatar() {
        return mTaskIdentityResponsible.getAvatarUrl();
    }

    /**
     * Calculates the days it takes from today until the deadline of the task is reached.
     *
     * @return the number of days until the deadline is reached
     */
    @Bindable
    public int getDaysToTaskDeadline() {
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
        return mCurrentIdentity.getObjectId().equals(mTaskIdentityResponsible.getObjectId());
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
}
