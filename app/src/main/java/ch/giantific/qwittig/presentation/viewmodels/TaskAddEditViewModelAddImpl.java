/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

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

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.TaskUser;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.ui.fragments.TaskAddEditFragment;
import ch.giantific.qwittig.utils.DateUtils;
import rx.SingleSubscriber;
import rx.Subscriber;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskAddEditViewModelAddImpl extends ViewModelBaseImpl<TaskAddEditViewModel.ViewListener>
        implements TaskAddEditViewModel {

    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_USERS_INVOLVED = "STATE_USERS_INVOLVED";
    Date mTaskDeadline;
    ArrayList<TaskUser> mTaskUsersInvolved;
    TaskRepository mTaskRepo;
    String mTaskTitle;
    private int mTaskTimeFrame;
    private List<ParseUser> mTaskUsersAvailable = new ArrayList<>();

    public TaskAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                       @NonNull UserRepository userRepository,
                                       @NonNull TaskRepository taskRepository) {
        super(savedState, userRepository);

        mTaskRepo = taskRepository;
        if (savedState != null) {
            mTaskDeadline = DateUtils.parseLongToDate(savedState.getLong(STATE_DEADLINE_SELECTED));
            ArrayList<TaskUser> usersInvolved = savedState.getParcelableArrayList(STATE_USERS_INVOLVED);
            mTaskUsersInvolved = usersInvolved != null ? usersInvolved : new ArrayList<TaskUser>();
        } else {
            mTaskDeadline = new Date();
            mTaskUsersInvolved = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putLong(STATE_DEADLINE_SELECTED, DateUtils.parseDateToLong(mTaskDeadline));
        outState.putParcelableArrayList(STATE_USERS_INVOLVED, mTaskUsersInvolved);
    }

    @Override
    public void attachView(@NonNull TaskAddEditViewModel.ViewListener view) {
        super.attachView(view);

        loadTaskUsers();
    }

    void loadTaskUsers() {
        mSubscriptions.add(mUserRepo.getUsersLocalAsync(mCurrentGroup)
                .toList()
                .subscribe(new Subscriber<List<User>>() {
                    @Override
                    public void onStart() {
                        super.onStart();

                        mTaskUsersAvailable.clear();
                    }

                    @Override
                    public void onCompleted() {
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<User> users) {
                        final int usersSize = users.size();
                        if (mTaskUsersInvolved.isEmpty()) {
                            for (int i = 0; i < usersSize; i++) {
                                final User user = users.get(i);
                                mTaskUsersAvailable.add(user);
                                mTaskUsersInvolved.add(new TaskUser(user.getObjectId(), true));
                            }
                        } else {
                            final int usersInvolvedSize = mTaskUsersInvolved.size();
                            final User[] userArray = new User[usersInvolvedSize];
                            final List<String> ids = new ArrayList<>(usersInvolvedSize);
                            for (int i = 0; i < usersInvolvedSize; i++) {
                                final TaskUser taskUser = mTaskUsersInvolved.get(i);
                                ids.add(taskUser.getUserId());
                            }

                            for (Iterator<User> iterator = users.iterator(); iterator.hasNext(); ) {
                                final User user = iterator.next();
                                final String userId = user.getObjectId();
                                if (ids.contains(userId)) {
                                    final int pos = ids.indexOf(userId);
                                    userArray[pos] = user;
                                    iterator.remove();
                                }
                            }

                            Collections.addAll(mTaskUsersAvailable, userArray);
                            for (User user : users) {
                                mTaskUsersAvailable.add(user);
                                mTaskUsersInvolved.add(new TaskUser(user.getObjectId(), false));
                            }
                        }

                        mView.setUserListMinimumHeight(usersSize);
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
    public User getUserAvailableAtPosition(int position) {
        return (User) mTaskUsersAvailable.get(position);
    }

    @Override
    public boolean isUserAtPositionInvolved(int position) {
        return mTaskUsersInvolved.get(position).isInvolved();
    }

    @Override
    public int getItemCount() {
        return mTaskUsersAvailable.size();
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
        final List<ParseUser> usersInvolved = getTaskUsersInvolved();
        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME) && usersInvolved.size() > 1) {
            mView.showMessage(R.string.toast_task_max_one_user_one_time);
            return;
        }

        final Task task = getTask(taskTitle, timeFrame, usersInvolved);
        mSubscriptions.add(mTaskRepo.saveTaskLocalAsync(task, Task.PIN_LABEL)
                .subscribe(new SingleSubscriber<Task>() {
                    @Override
                    public void onSuccess(Task value) {
                        task.saveEventually();
                        mView.finishScreen(TaskAddEditFragment.RESULT_TASK_SAVED);
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
                return Task.TIME_FRAME_DAILY;
            case R.string.time_frame_weekly:
                return Task.TIME_FRAME_WEEKLY;
            case R.string.time_frame_monthly:
                return Task.TIME_FRAME_MONTHLY;
            case R.string.time_frame_yearly:
                return Task.TIME_FRAME_YEARLY;
            case R.string.time_frame_one_time:
                return Task.TIME_FRAME_ONE_TIME;
            default:
                mTaskDeadline = null;
                return Task.TIME_FRAME_AS_NEEDED;
        }
    }

    @NonNull
    final List<ParseUser> getTaskUsersInvolved() {
        final List<ParseUser> parseUsers = new ArrayList<>();

        for (int i = 0, usersInvolvedSize = mTaskUsersInvolved.size(); i < usersInvolvedSize; i++) {
            TaskUser taskUser = mTaskUsersInvolved.get(i);
            if (taskUser.isInvolved()) {
                parseUsers.add(mTaskUsersAvailable.get(i));
            }
        }

        return parseUsers;
    }

    @NonNull
    Task getTask(@NonNull String taskTitle, @NonNull String timeFrame,
                 @NonNull List<ParseUser> usersInvolved) {
        return new Task(mCurrentUser, taskTitle, mCurrentGroup, timeFrame, mTaskDeadline,
                usersInvolved);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final int timeFrame = (int) parent.getItemAtPosition(position);
        setTaskTimeFrame(timeFrame);
        notifyPropertyChanged(BR.taskDeadlineVisibility);
    }

    @Override
    public void onUsersRowItemClick(int position) {
        TaskUser taskUser = mTaskUsersInvolved.get(position);
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
        for (TaskUser taskUser : mTaskUsersInvolved) {
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
        mView.finishScreen(TaskAddEditFragment.RESULT_TASK_DISCARDED);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mTaskUsersAvailable, fromPosition, toPosition);
        Collections.swap(mTaskUsersInvolved, fromPosition, toPosition);
        mView.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mTaskUsersAvailable.remove(position);
        mTaskUsersInvolved.remove(position);
        mView.notifyItemRemoved(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        setTaskDeadline(deadline);
    }
}
