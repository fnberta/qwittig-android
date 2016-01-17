/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Calendar;
import java.util.Date;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskRowViewModel extends BaseObservable {

    private Task mTask;
    private User mTaskUserResponsible;
    private User mCurrentUser;

    public TaskRowViewModel(@NonNull Task task, @NonNull User currentUser) {
        setTaskInfo(task, currentUser);
    }

    /**
     * Sets the task and the other info needed. Used to recycle view models.
     *
     * @param task        the task to display
     * @param currentUser the current user
     */
    public void setTaskInfo(@NonNull Task task, @NonNull User currentUser) {
        mTask = task;
        mTaskUserResponsible = task.getUserResponsible();
        mCurrentUser = currentUser;
        notifyPropertyChanged(BR.taskTitle);
        notifyPropertyChanged(BR.taskTimeFrame);
        notifyPropertyChanged(BR.taskUserResponsibleNickname);
        notifyPropertyChanged(BR.taskUserResponsibleAvatar);
        notifyPropertyChanged(BR.daysToTaskDeadline);
        notifyPropertyChanged(BR.taskLoading);
        notifyPropertyChanged(BR.currentUserResponsible);
        notifyPropertyChanged(BR.taskDoneText);
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
            case Task.TIME_FRAME_ONE_TIME:
                return R.string.time_frame_one_time;
            case Task.TIME_FRAME_AS_NEEDED:
                return R.string.time_frame_as_needed;
            case Task.TIME_FRAME_DAILY:
                return R.string.time_frame_daily;
            case Task.TIME_FRAME_WEEKLY:
                return R.string.time_frame_weekly;
            case Task.TIME_FRAME_MONTHLY:
                return R.string.time_frame_monthly;
            case Task.TIME_FRAME_YEARLY:
                return R.string.time_frame_yearly;
            default:
                throw new RuntimeException("Unsupported time frame " + timeFrame);
        }
    }

    @Bindable
    public String getTaskUserResponsibleNickname() {
        return mTaskUserResponsible.getNickname();
    }

    @Bindable
    public byte[] getTaskUserResponsibleAvatar() {
        return mTaskUserResponsible.getAvatar();
    }

    /**
     * Calculates the days it takes from today until the deadline of the task is reached.
     *
     * @return the number of days until the deadline is reached
     */
    @Bindable
    public int getDaysToTaskDeadline() {
        final Date deadlineDate = mTask.getDeadline();

        Calendar today = DateUtils.getCalendarInstanceUTC();
        Calendar deadline = DateUtils.getCalendarInstanceUTC();
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

    @Bindable
    public boolean isTaskLoading() {
        return mTask.isLoading();
    }

    @Bindable
    public boolean isCurrentUserResponsible() {
        return mCurrentUser.getObjectId().equals(mTaskUserResponsible.getObjectId());
    }

    @Bindable
    @StringRes
    public int getTaskDoneText() {
        final String timeFrame = mTask.getTimeFrame();
        switch (timeFrame) {
            case Task.TIME_FRAME_ONE_TIME:
                return R.string.task_done_single;
            case Task.TIME_FRAME_AS_NEEDED:
                return R.string.task_done_single;
            case Task.TIME_FRAME_DAILY:
                return R.string.task_done_today;
            case Task.TIME_FRAME_WEEKLY:
                return R.string.task_done_week;
            case Task.TIME_FRAME_MONTHLY:
                return R.string.task_done_month;
            case Task.TIME_FRAME_YEARLY:
                return R.string.task_done_year;
            default:
                return R.string.task_done_single;
        }
    }
}
