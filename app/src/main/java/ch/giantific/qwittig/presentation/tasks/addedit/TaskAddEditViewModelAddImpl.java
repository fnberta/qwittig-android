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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.addedit.models.TaskUser;
import ch.giantific.qwittig.utils.DateUtils;
import rx.SingleSubscriber;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Provides an implementation of the {@link TaskAddEditViewModel} interface for the add task screen.
 */
public class TaskAddEditViewModelAddImpl extends ListViewModelBaseImpl<Identity, TaskAddEditViewModel.ViewListener>
        implements TaskAddEditViewModel {

    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_IDENTITIES = "STATE_IDENTITIES";
    private static final String STATE_TITLE = "STATE_TITLE";
    final ArrayList<TaskUser> mTaskIdentities;
    final TaskRepository mTaskRepo;
    private final DateFormat mDateFormatter;
    Date mTaskDeadline;
    String mTaskTitle;
    private int mTaskTimeFrame;

    public TaskAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                       @NonNull TaskAddEditViewModel.ViewListener view,
                                       @NonNull UserRepository userRepository,
                                       @NonNull TaskRepository taskRepository) {
        super(savedState, view, userRepository);

        mTaskRepo = taskRepository;

        if (savedState != null) {
            mItems = new ArrayList<>();
            mTaskTitle = savedState.getString(STATE_TITLE);
            mTaskDeadline = new Date(savedState.getLong(STATE_DEADLINE_SELECTED));
            mTaskIdentities = savedState.getParcelableArrayList(STATE_IDENTITIES);
        } else {
            mTaskDeadline = new Date();
            mTaskIdentities = new ArrayList<>();
        }

        mDateFormatter = DateUtils.getDateFormatter(false);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putString(STATE_TITLE, mTaskTitle);
        outState.putLong(STATE_DEADLINE_SELECTED, mTaskDeadline.getTime());
        outState.putParcelableArrayList(STATE_IDENTITIES, mTaskIdentities);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.getIdentities(mCurrentIdentity.getGroup(), true)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        mItems.clear();

                        final int size = identities.size();
                        if (mTaskIdentities.isEmpty()) {
                            for (int i = 0; i < size; i++) {
                                final Identity identity = identities.get(i);
                                mItems.add(identity);
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

                            Collections.addAll(mItems, userArray);
                            for (Identity identity : identities) {
                                mItems.add(identity);
                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), false));
                            }
                        }

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
    public String getTaskDeadline() {
        return mDateFormatter.format(mTaskDeadline);
    }

    @Override
    public void setTaskDeadline(@NonNull Date deadline) {
        mTaskDeadline = deadline;
        notifyPropertyChanged(BR.taskDeadline);
    }

    @Override
    @Bindable
    public boolean isAsNeededTask() {
        return mTaskTimeFrame == R.string.time_frame_as_needed;
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
    public void onUpOrBackClick() {
        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mView.finishScreen(Activity.RESULT_CANCELED);
        }
    }

    boolean changesWereMade() {
        return !TextUtils.isEmpty(mTaskTitle);
    }

    @Override
    public float getIdentityAlpha(int position) {
        return mTaskIdentities.get(position).isInvolved() ? 1f : DISABLED_ALPHA;
    }

    @Override
    public void onTitleChanged(CharSequence s, int start, int before, int count) {
        mTaskTitle = s.toString();
    }

    @Override
    public void onDeadlineClicked(View view) {
        mView.showDatePickerDialog();
    }

    @Override
    public void onFabSaveTaskClick(View view) {
        if (TextUtils.isEmpty(mTaskTitle)) {
            mView.showMessage(R.string.error_task_title);
            return;
        }

        final String timeFrame = getTimeFrameSelected();
        final List<Identity> identities = getIdentitiesAvailable();
        if (Objects.equals(timeFrame, Task.TimeFrame.ONE_TIME) && identities.size() > 1) {
            mView.showMessage(R.string.toast_task_max_one_user_one_time);
            return;
        }

        final Task task = getTask(mTaskTitle, timeFrame, identities);
        getSubscriptions().add(mTaskRepo.saveTask(task)
                .subscribe(new SingleSubscriber<Task>() {
                    @Override
                    public void onSuccess(Task value) {
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
                identities.add(mItems.get(i));
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
        mTaskTimeFrame = (int) parent.getItemAtPosition(position);
        notifyPropertyChanged(BR.asNeededTask);
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
        Collections.swap(mItems, fromPosition, toPosition);
        Collections.swap(mTaskIdentities, fromPosition, toPosition);
        mView.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        mTaskIdentities.remove(position);
        mView.notifyItemRemoved(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        setTaskDeadline(deadline);
    }
}
