/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.parse.ParseUser;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.TaskHistory;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskDetailsViewModelImpl extends ListViewModelBaseImpl<TaskHistory, TaskDetailsViewModel.ViewListener>
        implements TaskDetailsViewModel {

    private Task mTask;
    private String mTaskId;
    private TaskRepository mTaskRepo;
    private String mTaskTitle;
    @StringRes
    private int mTaskTimeFrame;
    private SpannableStringBuilder mTaskUsersInvolved;
    private boolean mCurrentUserResponsible;

    public TaskDetailsViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull TaskDetailsViewModel.ViewListener view,
                                    @NonNull UserRepository userRepository,
                                    @NonNull GroupRepository groupRepository,
                                    @NonNull TaskRepository taskRepository,
                                    @NonNull String taskId) {
        super(savedState, view, groupRepository, userRepository);

        mTaskRepo = taskRepository;
        mTaskId = taskId;
    }

    @Override
    @Bindable
    public String getTaskTitle() {
        return mTaskTitle;
    }

    @Override
    public void setTaskTitle(@NonNull String taskTitle) {
        mTaskTitle = taskTitle;
        notifyPropertyChanged(BR.taskTitle);
    }

    @Override
    @StringRes
    @Bindable
    public int getTaskTimeFrame() {
        // TODO: crashes because is 0 at start and 0 is not a valid string res
        return mTaskTimeFrame;
    }

    @Override
    public void setTaskTimeFrame(@StringRes int taskTimeFrame) {
        mTaskTimeFrame = taskTimeFrame;
        notifyPropertyChanged(BR.taskTimeFrame);
    }

    @Override
    @Bindable
    public SpannableStringBuilder getTaskUsersInvolved() {
        return mTaskUsersInvolved;
    }

    @Override
    public void setTaskUsersInvolved(@NonNull SpannableStringBuilder taskUsersInvolved) {
        mTaskUsersInvolved = taskUsersInvolved;
        notifyPropertyChanged(BR.taskUsersInvolved);
    }

    @Override
    @Bindable
    public boolean isCurrentUserResponsible() {
        return mCurrentUserResponsible;
    }

    @Override
    public void setCurrentUserResponsible(boolean currentUserResponsible) {
        mCurrentUserResponsible = currentUserResponsible;
        notifyPropertyChanged(BR.currentUserResponsible);
    }

    @Override
    public TaskHistory getTaskHistoryForPosition(int position) {
        return mItems.get(position - 1);
    }

    @Override
    public void loadData() {
        mSubscriptions.add(mTaskRepo.getTaskLocalAsync(mTaskId)
                .flatMapObservable(new Func1<Task, Observable<TaskHistory>>() {
                    @Override
                    public Observable<TaskHistory> call(Task task) {
                        mTask = task;

                        updateToolbarHeader();
                        updateToolbarMenu();

                        return getTaskHistory();
                    }
                })
                .subscribe(new Subscriber<TaskHistory>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        Collections.sort(mItems, Collections.reverseOrder());
                        mView.notifyDataSetChanged();
                        mView.startPostponedEnterTransition();
                        setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_task_details_load);
                    }

                    @Override
                    public void onNext(TaskHistory taskHistory) {
                        mItems.add(taskHistory);
                    }
                })
        );
    }

    private Observable<TaskHistory> getTaskHistory() {
        final Map<String, List<Date>> history = mTask.getHistory();
        final Set<String> keys = history.keySet();
        return mUserRepo.getUsersLocalAsync(mCurrentGroup)
                .filter(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User user) {
                        return keys.contains(user.getObjectId());
                    }
                })
                .flatMap(new Func1<User, Observable<TaskHistory>>() {
                    @Override
                    public Observable<TaskHistory> call(final User user) {
                        return Observable.from(history.get(user.getObjectId()))
                                .map(new Func1<Date, TaskHistory>() {
                                    @Override
                                    public TaskHistory call(Date date) {
                                        return new TaskHistory(user, date);
                                    }
                                });
                    }
                });
    }

    private void updateToolbarHeader() {
        setTaskTitle(mTask.getTitle());
        updateTimeFrame();
        updateUsersInvolved();
    }

    private void updateTimeFrame() {
        String timeFrame = mTask.getTimeFrame();
        int timeFrameLocalized;
        switch (timeFrame) {
            case Task.TIME_FRAME_DAILY:
                timeFrameLocalized = R.string.time_frame_daily;
                break;
            case Task.TIME_FRAME_WEEKLY:
                timeFrameLocalized = R.string.time_frame_weekly;
                break;
            case Task.TIME_FRAME_MONTHLY:
                timeFrameLocalized = R.string.time_frame_monthly;
                break;
            case Task.TIME_FRAME_YEARLY:
                timeFrameLocalized = R.string.time_frame_yearly;
                break;
            case Task.TIME_FRAME_AS_NEEDED:
                timeFrameLocalized = R.string.time_frame_as_needed;
                break;
            case Task.TIME_FRAME_ONE_TIME:
                timeFrameLocalized = R.string.time_frame_one_time;
                break;
            default:
                timeFrameLocalized = -1;
        }
        if (timeFrameLocalized != -1) {
            setTaskTimeFrame(timeFrameLocalized);
        }
    }

    private void updateUsersInvolved() {
        final List<ParseUser> usersInvolved = mTask.getUsersInvolved();
        final User userResponsible = (User) usersInvolved.get(0);
        final boolean currentUserResponsible = mCurrentUser.getObjectId().equals(
                usersInvolved.get(0).getObjectId());
        setCurrentUserResponsible(currentUserResponsible);

        // TODO: build string in view model
        final SpannableStringBuilder stringBuilder = mView.buildUsersInvolvedString(usersInvolved,
                userResponsible, mCurrentUser);
        setTaskUsersInvolved(stringBuilder);
    }

    private void updateToolbarMenu() {
        User initiator = mTask.getInitiator();
        boolean showEditOptions = initiator.getObjectId().equals(mCurrentUser.getObjectId());

        if (showEditOptions) {
            List<ParseUser> usersInvolved = mTask.getUsersInvolved();
            for (ParseUser parseUser : usersInvolved) {
                User user = (User) parseUser;
                if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                    showEditOptions = false;
                    break;
                }
            }
        }

        mView.toggleEditOptions(showEditOptions);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void deleteTask() {
        mTask.deleteEventually();
        mView.finishScreen(RESULT_TASK_DELETED);
    }

    @Override
    public void editTask() {
        mView.startEditTaskActivity(mTaskId);
    }

    @Override
    public void onFabDoneClick(View view) {
        String timeFrame = mTask.getTimeFrame();

        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME)) {
            mTask.deleteEventually();
            mView.finishScreen(RESULT_TASK_DELETED);
            return;
        }

        mTask.updateDeadline();
        mTask.addHistoryEvent(mCurrentUser);
        mTask.saveEventually();

        updateToolbarHeader();
        mSubscriptions.add(getTaskHistory()
                .subscribe(new Subscriber<TaskHistory>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        Collections.sort(mItems, Collections.reverseOrder());
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_task_details_load);
                    }

                    @Override
                    public void onNext(TaskHistory taskHistory) {
                        mItems.add(taskHistory);
                    }
                })
        );
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onNewGroupSet() {
        mView.finishScreen(RESULT_GROUP_CHANGED);
    }
}
