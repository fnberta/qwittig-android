/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.tasks.addedit.models.TaskUser;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link TaskAddEditViewModel} interface for the edit task screen.
 */
public class TaskAddEditViewModelEditImpl extends TaskAddEditViewModelAddImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private final String mEditTaskId;
    private boolean mOldValuesSet;
    private Task mEditTask;

    public TaskAddEditViewModelEditImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository,
                                        @NonNull TaskRepository taskRepository,
                                        @NonNull String editTaskId) {
        super(savedState, navigator, eventBus, userRepository, taskRepository);

        mEditTaskId = editTaskId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
    }

    public void loadData() {
        getSubscriptions().add(mTaskRepo.fetchTaskData(mEditTaskId)
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
        super.loadData();
    }

    private void restoreOldValues() {
        setTaskTitle(mEditTask.getTitle());
        final String timeFrame = mEditTask.getTimeFrame();
        setTimeFrame(timeFrame);
        if (!Objects.equals(timeFrame, Task.TimeFrame.AS_NEEDED)) {
            setTaskDeadline(mEditTask.getDeadline());
        }

        setUsersInvolved(mEditTask.getIdentities());

        mOldValuesSet = true;
        mListInteraction.notifyDataSetChanged();
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
            notifyPropertyChanged(BR.selectedTimeFrame);
        }
    }

    private void setUsersInvolved(@NonNull List<Identity> identities) {
        for (Identity identity : identities) {
            mTaskIdentities.add(new TaskUser(identity.getObjectId(), true));
        }
    }

    @Override
    boolean changesWereMade() {
        if (!Objects.equals(mEditTask.getTitle(), mTaskTitle) ||
                !Objects.equals(mEditTask.getTimeFrame(), getTimeFrameSelected()) ||
                mEditTask.getDeadline().compareTo(mTaskDeadline) != 0) {
            return true;
        }

        final List<Identity> oldIdentities = mEditTask.getIdentities();
        final int oldIdentitiesSize = oldIdentities.size();
        final List<Identity> newIdentities = getIdentitiesAvailable();
        if (oldIdentitiesSize != newIdentities.size()) {
            return true;
        }

        for (int i = 0; i < oldIdentitiesSize; i++) {
            final Identity identityOld = oldIdentities.get(i);
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
