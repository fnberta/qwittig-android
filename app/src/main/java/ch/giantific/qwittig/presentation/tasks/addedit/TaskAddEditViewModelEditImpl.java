/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.tasks.addedit.itemmodels.TaskAddEditIdentityItemModel;

/**
 * Provides an implementation of the {@link TaskAddEditViewModel} interface for the edit task screen.
 */
public class TaskAddEditViewModelEditImpl extends TaskAddEditViewModelAddImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";

    private final String editTaskId;
    private boolean oldValuesSet;
    private Task editTask;

    public TaskAddEditViewModelEditImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepos,
                                        @NonNull TaskRepository taskRepo,
                                        @NonNull String editTaskId) {
        super(savedState, navigator, eventBus, userRepos, taskRepo);

        this.editTaskId = editTaskId;

        if (savedState != null) {
            oldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, oldValuesSet);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);


    }

//    public void loadData() {
//        getSubscriptions().add(taskRepo.fetchTaskData(editTaskId)
//                .subscribe(new SingleSubscriber<Task>() {
//                    @Override
//                    public void onSuccess(Task task) {
//                        editTask = task;
//
//                        if (!oldValuesSet) {
//                            restoreOldValues();
//                        }
//
//                        onOldTaskLoaded();
//                    }
//
//                    @Override
//                    public void onError(Throwable error) {
//                        // TODO: handle error
//                    }
//                })
//        );
//    }

//    private void onOldTaskLoaded() {
//        super.loadData();
//    }

    private void restoreOldValues() {
        setTitle(editTask.getTitle());
        final String timeFrame = editTask.getTimeFrame();
        handleTimeFrame(timeFrame);
        if (!Objects.equals(timeFrame, TimeFrame.AS_NEEDED)) {
            setDeadline(editTask.getDeadline());
        }

        final Set<String> identities = editTask.getIdentitiesIds();
        for (String identityId : identities) {
            for (TaskAddEditIdentityItemModel itemModel : items) {
                if (Objects.equals(itemModel.getIdentityId(), identityId)) {
                    itemModel.setInvolved(true);
                }
            }
        }

        oldValuesSet = true;
        listInteraction.notifyDataSetChanged();
    }

    private void handleTimeFrame(@NonNull @TimeFrame String timeFrame) {
        int res;
        switch (timeFrame) {
            case TimeFrame.ONE_TIME:
                res = R.string.time_frame_one_time;
                break;
            case TimeFrame.DAILY:
                res = R.string.time_frame_daily;
                break;
            case TimeFrame.WEEKLY:
                res = R.string.time_frame_weekly;
                break;
            case TimeFrame.MONTHLY:
                res = R.string.time_frame_monthly;
                break;
            case TimeFrame.YEARLY:
                res = R.string.time_frame_yearly;
                break;
            case TimeFrame.AS_NEEDED:
                res = R.string.time_frame_as_needed;
                break;
            default:
                res = -1;
        }

        if (res != -1) {
            setTimeFrame(res);
        }
    }

    @Override
    boolean changesWereMade() {
        if (!Objects.equals(editTask.getTitle(), title) ||
                !Objects.equals(editTask.getTimeFrame(), getTimeFrameSelected()) ||
                editTask.getDeadline().compareTo(deadline) != 0) {
            return true;
        }

        final Set<String> oldIdentities = editTask.getIdentitiesIds();
        final int oldIdentitiesSize = oldIdentities.size();
        final List<String> newIdentities = getIdentitiesAvailable();
        if (oldIdentitiesSize != newIdentities.size()) {
            return true;
        }

//        for (int i = 0; i < oldIdentitiesSize; i++) {
//            final String identityOld = oldIdentities.get(i);
//            final String identityNew = newIdentities.get(i);
//
//            if (!Objects.equals(identityOld, identityNew)) {
//                return true;
//            }
//        }

        return false;
    }

//    @NonNull
//    @Override
//    Task2 getTask(@NonNull String taskTitle, @NonNull String timeFrame, @NonNull List<String> identities) {
//        mEditsetTitle(title);
//        mEditsetTimeFrame(timeFrame);
//        mEditsetDeadlineResetMidnight(deadline);
//        mEditsetIdentities(identities);
//        return editTask;
//    }
}
