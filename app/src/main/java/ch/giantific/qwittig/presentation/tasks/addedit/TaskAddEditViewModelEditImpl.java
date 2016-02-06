/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.DateUtils;
import rx.SingleSubscriber;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskAddEditViewModelEditImpl extends TaskAddEditViewModelAddImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String STATE_OLD_TITLE = "STATE_OLD_TITLE";
    private static final String STATE_OLD_TIME_FRAME = "STATE_OLD_TIME_FRAME";
    private static final String STATE_OLD_DEADLINE = "STATE_OLD_DEADLINE";
    private boolean mOldValuesSet;
    private Task mEditTask;
    private String mEditTaskId;
    private String mOldTaskTitle;
    private String mOldTaskTimeFrame;
    private Date mOldTaskDeadline;
    private List<Identity> mOldTaskIdentities;

    public TaskAddEditViewModelEditImpl(@Nullable Bundle savedState,
                                        @NonNull TaskAddEditViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository,
                                        @NonNull IdentityRepository identityRepository,
                                        @NonNull TaskRepository taskRepository,
                                        @NonNull String editTaskId) {
        super(savedState, view, identityRepository, userRepository, taskRepository);

        mEditTaskId = editTaskId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
            mOldTaskTitle = savedState.getString(STATE_OLD_TITLE, "");
            mOldTaskTimeFrame = savedState.getString(STATE_OLD_TIME_FRAME, "");
            mOldTaskDeadline = DateUtils.parseLongToDate(savedState.getLong(STATE_OLD_DEADLINE));
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
        outState.putString(STATE_OLD_TITLE, mOldTaskTitle);
        outState.putString(STATE_OLD_TIME_FRAME, mOldTaskTimeFrame);
        outState.putLong(STATE_OLD_DEADLINE, DateUtils.parseDateToLong(mOldTaskDeadline));
    }

    @Override
    void loadTaskUsers() {
        mSubscriptions.add(mTaskRepo.fetchTaskDataLocalAsync(mEditTaskId)
                .subscribe(new SingleSubscriber<Task>() {
                    @Override
                    public void onSuccess(Task task) {
                        mEditTask = task;

                        if (!mOldValuesSet) {
                            restoreOldValues();
                        }

                        onOldTaskLoaded();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                })
        );
    }

    private void onOldTaskLoaded() {
        super.loadTaskUsers();
    }

    private void restoreOldValues() {
        mOldTaskTitle = mEditTask.getTitle();
        setTaskTitle(mOldTaskTitle);

        mOldTaskTimeFrame = mEditTask.getTimeFrame();
        setTimeFrame(mOldTaskTimeFrame);

        if (!mOldTaskTitle.equals(Task.TimeFrame.AS_NEEDED)) {
            mOldTaskDeadline = mEditTask.getDeadline();
            setTaskDeadline(mOldTaskDeadline);
        }

        mOldTaskIdentities = mEditTask.getIdentities();
        setUsersInvolved(mOldTaskIdentities);

        mOldValuesSet = true;
        mView.notifyDataSetChanged();
    }

    private void setTimeFrame(@NonNull @Task.TimeFrame String timeFrame) {
        int res;
        switch (timeFrame) {
            case Task.TimeFrame.ONE_TIME:
                res = R.string.time_frame_one_time;
                break;
            case Task.TimeFrame.DAILY:
                res = R.string.time_frame_daily;
                break;
            case Task.TimeFrame.WEEKLY:
                res = R.string.time_frame_weekly;
                break;
            case Task.TimeFrame.MONTHLY:
                res = R.string.time_frame_monthly;
                break;
            case Task.TimeFrame.YEARLY:
                res = R.string.time_frame_yearly;
                break;
            case Task.TimeFrame.AS_NEEDED:
                res = R.string.time_frame_as_needed;
                break;
            default:
                res = -1;
        }

        if (res != -1) {
            mView.setTimeFramePosition(res);
        }
    }

    private void setUsersInvolved(@NonNull List<Identity> identities) {
        for (Identity identity : identities) {
            mTaskIdentities.add(new TaskUser(identity.getObjectId(), true));
        }
    }

    @Override
    boolean changesWereMade() {
        if (!mOldTaskTitle.equals(mView.getTaskTitle()) ||
                !mOldTaskTimeFrame.equals(getTimeFrameSelected()) ||
                mOldTaskDeadline.compareTo(mTaskDeadline) != 0) {
            return true;
        }

        if (mOldTaskIdentities == null) {
            mOldTaskIdentities = mEditTask.getIdentities();
        }

        final int oldIdentitiesSize = mOldTaskIdentities.size();
        final List<Identity> newIdentities = getIdentitiesAvailable();
        if (oldIdentitiesSize != newIdentities.size()) {
            return true;
        }

        for (int i = 0; i < oldIdentitiesSize; i++) {
            final Identity identityOld = mOldTaskIdentities.get(i);
            final Identity identityNew = newIdentities.get(i);

            if (!identityOld.getObjectId().equals(identityNew.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    @Override
    Task getTask(@NonNull String title, @NonNull String timeFrame, @NonNull List<Identity> identities) {
        mEditTask.setTitle(title);
        mEditTask.setTimeFrame(timeFrame);
        mEditTask.setDeadlineResetMidnight(mTaskDeadline);
        mEditTask.setIdentities(identities);
        return mEditTask;
    }
}
