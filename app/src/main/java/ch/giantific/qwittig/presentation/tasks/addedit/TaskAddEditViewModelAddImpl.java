/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.DateUtils;
import rx.SingleSubscriber;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskAddEditViewModelAddImpl extends ViewModelBaseImpl<TaskAddEditViewModel.ViewListener>
        implements TaskAddEditViewModel {

    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_USERS_INVOLVED = "STATE_USERS_INVOLVED";
    Date mTaskDeadline;
    ArrayList<TaskUser> mTaskIdentities;
    TaskRepository mTaskRepo;
    String mTaskTitle;
    private IdentityRepository mIdentityRepo;
    private int mTaskTimeFrame;
    private List<Identity> mIdentitiesAvailable = new ArrayList<>();

    public TaskAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                       @NonNull TaskAddEditViewModel.ViewListener view,
                                       @NonNull IdentityRepository identityRepository,
                                       @NonNull UserRepository userRepository,
                                       @NonNull TaskRepository taskRepository) {
        super(savedState, view, userRepository);

        mIdentityRepo = identityRepository;
        mTaskRepo = taskRepository;

        if (savedState != null) {
            mTaskDeadline = DateUtils.parseLongToDate(savedState.getLong(STATE_DEADLINE_SELECTED));
            ArrayList<TaskUser> usersInvolved = savedState.getParcelableArrayList(STATE_USERS_INVOLVED);
            mTaskIdentities = usersInvolved != null ? usersInvolved : new ArrayList<TaskUser>();
        } else {
            mTaskDeadline = new Date();
            mTaskIdentities = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putLong(STATE_DEADLINE_SELECTED, DateUtils.parseDateToLong(mTaskDeadline));
        outState.putParcelableArrayList(STATE_USERS_INVOLVED, mTaskIdentities);
    }

    @Override
    public void onStart() {
        super.onStart();

        loadTaskUsers();
    }

    void loadTaskUsers() {
        mSubscriptions.add(mIdentityRepo.getIdentitiesLocalAsync(mCurrentIdentity.getGroup())
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        mIdentitiesAvailable.clear();

                        final int size = identities.size();
                        if (mTaskIdentities.isEmpty()) {
                            for (int i = 0; i < size; i++) {
                                final Identity identity = identities.get(i);
                                mIdentitiesAvailable.add(identity);
                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), true));
                            }
                        } else {
                            final int sizeInvolved = mTaskIdentities.size();
                            final Identity[] userArray = new Identity[sizeInvolved];
                            final List<String> ids = new ArrayList<>(sizeInvolved);
                            for (int i = 0; i < sizeInvolved; i++) {
                                final TaskUser taskUser = mTaskIdentities.get(i);
                                ids.add(taskUser.getIdentityId());
                            }

                            for (Iterator<Identity> iterator = identities.iterator(); iterator.hasNext(); ) {
                                final Identity identity = iterator.next();
                                final String userId = identity.getObjectId();
                                if (ids.contains(userId)) {
                                    final int pos = ids.indexOf(userId);
                                    userArray[pos] = identity;
                                    iterator.remove();
                                }
                            }

                            Collections.addAll(mIdentitiesAvailable, userArray);
                            for (Identity identity : identities) {
                                mIdentitiesAvailable.add(identity);
                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), false));
                            }
                        }

                        mView.setUserListMinimumHeight(size);
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO:handle error
                    }
                })
        );
    }

    @Override
    public String getTaskTitle() {
        return mTaskTitle;
    }

    @Override
    public void setTaskTitle(@NonNull String taskTitle) {
        mTaskTitle = taskTitle;
        notifyPropertyChanged(BR.taskTitle);
    }

    @Override
    @Bindable
    public Date getTaskDeadline() {
        return mTaskDeadline;
    }

    @Override
    public void setTaskDeadline(@NonNull Date deadline) {
        mTaskDeadline = deadline;
        notifyPropertyChanged(BR.taskDeadline);
    }

    @Override
    @Bindable
    public int getTaskDeadlineVisibility() {
        return mTaskTimeFrame == R.string.time_frame_as_needed ? View.GONE : View.VISIBLE;
    }

    @Override
    @Bindable
    public int getTaskTimeFrame() {
        return mTaskTimeFrame;
    }

    @Override
    public void setTaskTimeFrame(int taskTimeFrame) {
        mTaskTimeFrame = taskTimeFrame;
        notifyPropertyChanged(BR.taskTimeFrame);
    }

    @Override
    public void checkForChangesAndExit() {
        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mView.finishScreen(Activity.RESULT_CANCELED);
        }
    }

    boolean changesWereMade() {
        return !TextUtils.isEmpty(mView.getTaskTitle());
    }

    @Override
    public Identity getIdentityAtPosition(int position) {
        return mIdentitiesAvailable.get(position);
    }

    @Override
    public boolean isUserAtPositionInvolved(int position) {
        return mTaskIdentities.get(position).isInvolved();
    }

    @Override
    public int getItemCount() {
        return mIdentitiesAvailable.size();
    }

    @Override
    public void onDeadlineClicked(View view) {
        mView.showDatePickerDialog();
    }

    @Override
    public void onFabSaveTaskClick(View view) {
        final String taskTitle = mView.getTaskTitle();
        if (TextUtils.isEmpty(taskTitle)) {
            mView.showMessage(R.string.error_task_title);
            return;
        }

        final String timeFrame = getTimeFrameSelected();
        final List<Identity> identities = getIdentitiesAvailable();
        if (timeFrame.equals(Task.TimeFrame.ONE_TIME) && identities.size() > 1) {
            mView.showMessage(R.string.toast_task_max_one_user_one_time);
            return;
        }

        final Task task = getTask(taskTitle, timeFrame, identities);
        mSubscriptions.add(mTaskRepo.saveTaskLocalAsync(task, Task.PIN_LABEL)
                .subscribe(new SingleSubscriber<Task>() {
                    @Override
                    public void onSuccess(Task value) {
                        task.saveEventually();
                        mView.finishScreen(TaskAddEditViewModel.TaskResult.TASK_SAVED);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_task_save);
                    }
                }));
    }

    @Task.TimeFrame
    final String getTimeFrameSelected() {
        switch (mTaskTimeFrame) {
            case R.string.time_frame_daily:
                return Task.TimeFrame.DAILY;
            case R.string.time_frame_weekly:
                return Task.TimeFrame.WEEKLY;
            case R.string.time_frame_monthly:
                return Task.TimeFrame.MONTHLY;
            case R.string.time_frame_yearly:
                return Task.TimeFrame.YEARLY;
            case R.string.time_frame_one_time:
                return Task.TimeFrame.ONE_TIME;
            default:
                mTaskDeadline = null;
                return Task.TimeFrame.AS_NEEDED;
        }
    }

    @NonNull
    final List<Identity> getIdentitiesAvailable() {
        final List<Identity> identities = new ArrayList<>();

        for (int i = 0, usersInvolvedSize = mTaskIdentities.size(); i < usersInvolvedSize; i++) {
            TaskUser taskUser = mTaskIdentities.get(i);
            if (taskUser.isInvolved()) {
                identities.add(mIdentitiesAvailable.get(i));
            }
        }

        return identities;
    }

    @NonNull
    Task getTask(@NonNull String taskTitle, @NonNull String timeFrame,
                 @NonNull List<Identity> identities) {
        return new Task(mCurrentIdentity, taskTitle, mCurrentIdentity.getGroup(), timeFrame,
                mTaskDeadline, identities);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final int timeFrame = (int) parent.getItemAtPosition(position);
        setTaskTimeFrame(timeFrame);
        notifyPropertyChanged(BR.taskDeadlineVisibility);
    }

    @Override
    public void onUsersRowItemClick(int position) {
        TaskUser taskUser = mTaskIdentities.get(position);
        if (taskUser.isInvolved()) {
            if (!userIsLastOneChecked()) {
                taskUser.setIsInvolved(false);
                mView.notifyItemChanged(position);
            } else {
                mView.showMessage(R.string.toast_min_one_user);
            }
        } else {
            taskUser.setIsInvolved(true);
            mView.notifyItemChanged(position);
        }
    }

    private boolean userIsLastOneChecked() {
        int usersInvolvedCount = 0;
        for (TaskUser taskUser : mTaskIdentities) {
            if (taskUser.isInvolved()) {
                usersInvolvedCount++;
            }

            if (usersInvolvedCount > 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        mView.onStartUserDrag(viewHolder);
    }

    @Override
    public void onDiscardChangesSelected() {
        mView.finishScreen(TaskAddEditViewModel.TaskResult.TASK_DISCARDED);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mIdentitiesAvailable, fromPosition, toPosition);
        Collections.swap(mTaskIdentities, fromPosition, toPosition);
        mView.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mIdentitiesAvailable.remove(position);
        mTaskIdentities.remove(position);
        mView.notifyItemRemoved(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        setTaskDeadline(deadline);
    }
}
