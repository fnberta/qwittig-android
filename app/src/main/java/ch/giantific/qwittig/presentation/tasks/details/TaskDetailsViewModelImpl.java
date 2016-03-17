/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.view.View;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.details.items.DetailsItem;
import ch.giantific.qwittig.presentation.tasks.details.items.HeaderItem;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskHistoryItem;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskDetailsViewModelImpl extends ListViewModelBaseImpl<DetailsItem, TaskDetailsViewModel.ViewListener>
        implements TaskDetailsViewModel {

    private final String mTaskId;
    private final TaskRepository mTaskRepo;
    private Task mTask;
    private String mTaskTitle;
    @StringRes
    private int mTaskTimeFrame;
    private SpannableStringBuilder mTaskUsersInvolved;
    private boolean mCurrentUserResponsible;

    public TaskDetailsViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull TaskDetailsViewModel.ViewListener view,
                                    @NonNull UserRepository userRepository,
                                    @NonNull TaskRepository taskRepository,
                                    @NonNull String taskId) {
        super(savedState, view, userRepository);

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
    public void loadData() {
        getSubscriptions().add(mTaskRepo.getTask(mTaskId)
                .flatMapObservable(new Func1<Task, Observable<TaskHistoryItem>>() {
                    @Override
                    public Observable<TaskHistoryItem> call(Task task) {
                        mTask = task;

                        updateToolbarHeader();
                        updateToolbarMenu();

                        return getTaskHistory();
                    }
                })
                .subscribe(new Subscriber<TaskHistoryItem>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                        mItems.add(new HeaderItem(R.string.header_task_history));
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
                    public void onNext(TaskHistoryItem taskHistoryItem) {
                        mItems.add(taskHistoryItem);
                    }
                })
        );
    }

    private Observable<TaskHistoryItem> getTaskHistory() {
        final Map<String, List<Date>> history = mTask.getHistory();
        final Set<String> keys = history.keySet();
        return mUserRepo.getIdentities(mCurrentIdentity.getGroup(), true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return keys.contains(identity.getObjectId());
                    }
                })
                .flatMap(new Func1<Identity, Observable<TaskHistoryItem>>() {
                    @Override
                    public Observable<TaskHistoryItem> call(final Identity identity) {
                        return Observable.from(history.get(identity.getObjectId()))
                                .map(new Func1<Date, TaskHistoryItem>() {
                                    @Override
                                    public TaskHistoryItem call(Date date) {
                                        return new TaskHistoryItem(identity, date);
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
            case Task.TimeFrame.DAILY:
                timeFrameLocalized = R.string.time_frame_daily;
                break;
            case Task.TimeFrame.WEEKLY:
                timeFrameLocalized = R.string.time_frame_weekly;
                break;
            case Task.TimeFrame.MONTHLY:
                timeFrameLocalized = R.string.time_frame_monthly;
                break;
            case Task.TimeFrame.YEARLY:
                timeFrameLocalized = R.string.time_frame_yearly;
                break;
            case Task.TimeFrame.AS_NEEDED:
                timeFrameLocalized = R.string.time_frame_as_needed;
                break;
            case Task.TimeFrame.ONE_TIME:
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
        final List<Identity> identities = mTask.getIdentities();
        final Identity identityResponsible = identities.get(0);
        final boolean currentIdentityResponsible = mCurrentIdentity.getObjectId().equals(
                identities.get(0).getObjectId());
        setCurrentUserResponsible(currentIdentityResponsible);

        // TODO: build string in view model
        final SpannableStringBuilder stringBuilder = mView.buildUsersInvolvedString(identities,
                identityResponsible);
        setTaskUsersInvolved(stringBuilder);
    }

    private void updateToolbarMenu() {
        final Identity initiator = mTask.getInitiator();
        boolean showEditOptions = initiator.getObjectId().equals(mCurrentUser.getObjectId());

        if (showEditOptions) {
            List<Identity> identities = mTask.getIdentities();
            for (Identity identity : identities) {
                if (!identity.isActive()) {
                    showEditOptions = false;
                    break;
                }
            }
        }

        mView.toggleEditOptions(showEditOptions);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void deleteTask() {
        mTask.deleteEventually();
        mView.finishScreen(TaskDetailsResult.TASK_DELETED);
    }

    @Override
    public void editTask() {
        mView.startEditTaskActivity(mTaskId);
    }

    @Override
    public void onFabDoneClick(View view) {
        String timeFrame = mTask.getTimeFrame();

        if (timeFrame.equals(Task.TimeFrame.ONE_TIME)) {
            mTask.deleteEventually();
            mView.finishScreen(TaskDetailsResult.TASK_DELETED);
            return;
        }

        mTask.updateDeadline();
        mTask.addHistoryEvent(mCurrentIdentity);
        mTask.saveEventually();

        updateToolbarHeader();
        getSubscriptions().add(getTaskHistory()
                .subscribe(new Subscriber<TaskHistoryItem>() {
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
                    public void onNext(TaskHistoryItem taskHistoryItem) {
                        mItems.add(taskHistoryItem);
                    }
                })
        );
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onIdentitySelected() {
        mView.finishScreen(TaskDetailsResult.GROUP_CHANGED);
    }
}
