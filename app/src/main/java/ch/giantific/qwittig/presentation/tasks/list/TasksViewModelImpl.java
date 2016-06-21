/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.list.items.TaskItem;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem.Type;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksHeaderItem;
import ch.giantific.qwittig.presentation.tasks.list.models.TaskDeadline;
import ch.giantific.qwittig.utils.MessageAction;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link TasksViewModel} interface.
 */
public class TasksViewModelImpl extends OnlineListViewModelBaseImpl<TasksBaseItem, TasksViewModel.ViewListener>
        implements TasksViewModel {

    private static final String STATE_LOADING_TASKS = "STATE_LOADING_TASKS";
    private static final String STATE_DEADLINE = "STATE_DEADLINE";
    private final Navigator mNavigator;
    private final TaskRepository mTaskRepo;
    private ArrayList<String> mLoadingTasks;
    private TaskDeadline mDeadline;

    public TasksViewModelImpl(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull RxBus<Object> eventBus,
                              @NonNull UserRepository userRepository,
                              @NonNull TaskRepository taskRepo,
                              @NonNull TaskDeadline deadline) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mTaskRepo = taskRepo;
        if (savedState != null) {
            mItems = new ArrayList<>();
            mDeadline = savedState.getParcelable(STATE_DEADLINE);
            mLoadingTasks = savedState.getStringArrayList(STATE_LOADING_TASKS);
        } else {
            mDeadline = deadline;
            mLoadingTasks = new ArrayList<>();
        }
    }

    @VisibleForTesting
    public void setLoadingTasks(@NonNull ArrayList<String> loadingTasks) {
        mLoadingTasks = loadingTasks;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_DEADLINE, mDeadline);
        outState.putStringArrayList(STATE_LOADING_TASKS, mLoadingTasks);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(Identity identity) {
                        return mTaskRepo.getTasks(identity, mDeadline.getDate());
                    }
                })
                .filter(new Func1<Task, Boolean>() {
                    @Override
                    public Boolean call(Task task) {
                        final List<Identity> identities = task.getIdentities();
                        return !identities.isEmpty() && identities.contains(mCurrentIdentity);
                    }
                })
                .subscribe(new Subscriber<Task>() {
                    List<TasksBaseItem> tasksUser;
                    List<TasksBaseItem> tasksGroup;

                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                        tasksUser = new ArrayList<>();
                        tasksGroup = new ArrayList<>();
                    }

                    @Override
                    public void onCompleted() {
                        if (!tasksUser.isEmpty()) {
                            mItems.add(new TasksHeaderItem(R.string.task_header_my));
                            mItems.addAll(tasksUser);
                        }
                        if (!tasksGroup.isEmpty()) {
                            mItems.add(new TasksHeaderItem(R.string.task_header_group));
                            mItems.addAll(tasksGroup);
                        }

                        setLoading(false);
                        mListInteraction.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_tasks_load);
                    }

                    @Override
                    public void onNext(Task task) {
                        final Identity identityResponsible = task.getIdentityResponsible();
                        if (Objects.equals(mCurrentIdentity.getObjectId(), identityResponsible.getObjectId())) {
                            tasksUser.add(new TaskItem(task, mCurrentIdentity));
                        } else {
                            tasksGroup.add(new TaskItem(task, mCurrentIdentity));
                        }

                        task.setLoading(mLoadingTasks.contains(task.getObjectId()));
                    }
                })
        );
    }

    @Override
    public void onDataUpdated(boolean successful) {
        setRefreshing(false);
        if (successful) {
            loadData();
        } else {
            mView.showMessageWithAction(R.string.toast_error_tasks_update, getRefreshAction());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public void onAddTaskFabClick(View view) {
        mNavigator.startTaskAdd();
    }

    @Override
    public void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final TaskDeadline deadline = (TaskDeadline) parent.getItemAtPosition(position);
        if (!Objects.equals(deadline, mDeadline)) {
            mDeadline = deadline;
            loadData();
        }
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.startUpdateTasksService();
    }

    @NonNull
    private MessageAction getRefreshAction() {
        return new MessageAction(R.string.action_retry) {
            @Override
            public void onClick(View v) {
                refreshItems();
            }
        };
    }

    @Override
    public void onTaskRowClicked(int position) {
        final Task task = ((TaskItem) mItems.get(position)).getTask();
        mNavigator.startTaskDetails(task);
    }

    @Override
    public void onDoneButtonClicked(int position) {
        final TaskItem taskItem = (TaskItem) mItems.get(position);
        final Task task = taskItem.getTask();
        final String timeFrame = task.getTimeFrame();

        if (Objects.equals(timeFrame, Task.TimeFrame.ONE_TIME)) {
            task.deleteEventually();
            mItems.remove(taskItem);
            mListInteraction.notifyItemRemoved(position);
            return;
        }

        final TaskHistoryEvent newEvent = new TaskHistoryEvent(task, mCurrentIdentity, new Date());
        getSubscriptions().add(Completable.fromSingle(mTaskRepo.saveTaskHistoryEvent(newEvent)).subscribe());

        final Identity identityResponsible = task.getIdentityResponsible();
        final Identity identityResponsibleNew = task.handleHistoryEvent();
        task.saveEventually();

        final String currentIdentityId = mCurrentIdentity.getObjectId();
        if (identityResponsible != null && Objects.equals(identityResponsible.getObjectId(), currentIdentityId) ||
                identityResponsibleNew != null && identityResponsibleNew.getObjectId().equals(currentIdentityId)) {
            loadData();
        } else {
            mListInteraction.notifyItemChanged(position);
        }
    }

    @Override
    public void onRemindButtonClicked(int position) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        final TaskItem taskItem = (TaskItem) mItems.get(position);
        final Task task = taskItem.getTask();
        final String taskId = task.getObjectId();
        if (mLoadingTasks.contains(taskId)) {
            return;
        }

        setTaskLoading(task, taskId, position, true);
        mView.loadRemindUserWorker(taskId);
    }

    private void setTaskLoading(@NonNull Task task, @NonNull String objectId, int position,
                                boolean isLoading) {
        task.setLoading(isLoading);
        mListInteraction.notifyItemChanged(position);

        if (isLoading) {
            mLoadingTasks.add(objectId);
        } else {
            mLoadingTasks.remove(objectId);
        }
    }

    @Override
    public void setTaskReminderStream(@NonNull Single<String> single,
                                      @NonNull final String taskId,
                                      @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {
                        mView.removeWorker(workerTag);
                        final Task task = stopTaskLoading(taskId);
                        if (task != null) {
                            final Identity identityResponsible = task.getIdentities().get(0);
                            final String nickname = identityResponsible.getNickname();
                            mView.showMessage(R.string.toast_task_reminded_user, nickname);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessage(mTaskRepo.getErrorMessage(error));
                        stopTaskLoading(taskId);
                    }
                })
        );
    }

    @Nullable
    private Task stopTaskLoading(@NonNull String taskId) {
        for (int i = 0, tasksSize = mItems.size(); i < tasksSize; i++) {
            final TasksBaseItem taskListItem = mItems.get(i);
            if (taskListItem.getType() != Type.TASK) {
                continue;
            }

            final TaskItem taskItem = (TaskItem) taskListItem;
            final Task task = taskItem.getTask();
            if (Objects.equals(taskId, task.getObjectId())) {
                setTaskLoading(task, taskId, i, false);
                return task;
            }
        }

        return null;
    }
}
