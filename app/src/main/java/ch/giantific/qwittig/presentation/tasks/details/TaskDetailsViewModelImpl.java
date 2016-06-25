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
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsItemModel;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsHeaderItem;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsHistoryItem;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link TaskDetailsViewModel} interface.
 */
public class TaskDetailsViewModelImpl extends ListViewModelBaseImpl<TaskDetailsItemModel, TaskDetailsViewModel.ViewListener>
        implements TaskDetailsViewModel {

    private final String mTaskId;
    private final Navigator mNavigator;
    private final TaskRepository mTaskRepo;
    private Task mTask;
    @StringRes
    private int mTaskTimeFrame;
    private SpannableStringBuilder mTaskIdentities;
    private boolean mCurrentUserResponsible;

    public TaskDetailsViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull TaskRepository taskRepository,
                                    @NonNull String taskId) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mTaskRepo = taskRepository;
        mTaskId = taskId;
    }

    @Override
    @Bindable
    public String getTaskTitle() {
        if (mTask != null) {
            return mTask.getTitle();
        }

        return "";
    }

    @Override
    @StringRes
    @Bindable
    public int getTaskTimeFrame() {
        return mTaskTimeFrame;
    }

    @Override
    public void setTaskTimeFrame(@StringRes int taskTimeFrame) {
        mTaskTimeFrame = taskTimeFrame;
        notifyPropertyChanged(BR.taskTimeFrame);
    }

    @Bindable
    public SpannableStringBuilder getTaskIdentities() {
        return mTaskIdentities;
    }

    public void setTaskIdentities(@NonNull SpannableStringBuilder taskIdentities) {
        mTaskIdentities = taskIdentities;
        notifyPropertyChanged(BR.taskIdentities);
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
                .flatMapObservable(new Func1<Task, Observable<TaskHistoryEvent>>() {
                    @Override
                    public Observable<TaskHistoryEvent
                            > call(Task task) {
                        mTask = task;

                        updateToolbarHeader();
                        updateToolbarMenu();

                        return mTaskRepo.getTaskHistoryEvents(task);
                    }
                })
                .map(new Func1<TaskHistoryEvent, TaskDetailsHistoryItem>() {
                    @Override
                    public TaskDetailsHistoryItem call(TaskHistoryEvent taskHistoryEvent) {
                        return new TaskDetailsHistoryItem(taskHistoryEvent);
                    }
                })
                .subscribe(new Subscriber<TaskDetailsHistoryItem>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        Collections.sort(mItems, Collections.reverseOrder());
                        mItems.add(0, new TaskDetailsHeaderItem(R.string.header_task_history));
                        mListInteraction.notifyDataSetChanged();
                        setLoading(false);
                        mView.startPostponedEnterTransition();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        mView.startPostponedEnterTransition();
                        mView.showMessage(R.string.toast_error_task_details_load);
                    }

                    @Override
                    public void onNext(TaskDetailsHistoryItem taskHistoryItem) {
                        mItems.add(taskHistoryItem);
                    }
                })
        );
    }

    private void updateToolbarHeader() {
        notifyPropertyChanged(BR.taskTitle);
        updateTimeFrame();
        updateIdentities();
    }

    private void updateTimeFrame() {
        final String timeFrame = mTask.getTimeFrame();
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

    private void updateIdentities() {
        final List<Identity> identities = mTask.getIdentities();
        final Identity identityResponsible = identities.get(0);
        setCurrentUserResponsible(Objects.equals(mCurrentIdentity.getObjectId(), identityResponsible.getObjectId()));

        final SpannableStringBuilder stringBuilder = mView.buildTaskIdentitiesString(identities,
                identityResponsible);
        setTaskIdentities(stringBuilder);
    }

    private void updateToolbarMenu() {
        final Identity initiator = mTask.getInitiator();
        boolean showEditOptions = Objects.equals(initiator.getObjectId(), mCurrentIdentity.getObjectId());

        if (showEditOptions) {
            final List<Identity> identities = mTask.getIdentities();
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
    public boolean isEmpty() {
        for (TaskDetailsItemModel item : mItems) {
            if (item.getType() == TaskDetailsItemModel.Type.HISTORY) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void deleteTask() {
        mTask.deleteEventually();
        mNavigator.finish(TaskDetailsResult.TASK_DELETED);
    }

    @Override
    public void editTask() {
        mNavigator.startTaskEdit(mTaskId);
    }

    @Override
    public void onFabDoneClick(View view) {
        final String timeFrame = mTask.getTimeFrame();
        if (Objects.equals(timeFrame, Task.TimeFrame.ONE_TIME)) {
            mTask.deleteEventually();
            mNavigator.finish(TaskDetailsResult.TASK_DELETED);
            return;
        }

        final TaskHistoryEvent newEvent = new TaskHistoryEvent(mTask, mCurrentIdentity, new Date());
        getSubscriptions().add(mTaskRepo.saveTaskHistoryEvent(newEvent)
                .subscribe(new SingleSubscriber<TaskHistoryEvent>() {
                    @Override
                    public void onSuccess(TaskHistoryEvent event) {
                        mTask.handleHistoryEvent();
                        mTask.saveEventually();

                        updateToolbarHeader();
                        mItems.add(new TaskDetailsHistoryItem(event));
                        mListInteraction.notifyItemInserted(mItems.size());
                        notifyPropertyChanged(BR.empty);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_task_done);
                    }
                })
        );
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        mNavigator.finish(TaskDetailsResult.GROUP_CHANGED);
    }
}
