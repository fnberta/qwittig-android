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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 09.01.16.
 */
public class TasksViewModelImpl extends OnlineListViewModelBaseImpl<Task, TasksViewModel.ViewListener>
        implements TasksViewModel {

    private static final String STATE_LOADING_TASKS = "STATE_LOADING_TASKS";
    private TaskRepository mTaskRepo;
    private ArrayList<String> mLoadingTasks;
    private Date mDeadlineSelected;

    public TasksViewModelImpl(@Nullable Bundle savedState,
                              @NonNull TasksViewModel.ViewListener view,
                              @NonNull IdentityRepository identityRepository,
                              @NonNull UserRepository userRepository,
                              @NonNull TaskRepository taskRepo) {
        super(savedState, view, identityRepository, userRepository);

        mTaskRepo = taskRepo;
        mDeadlineSelected = new Date(Long.MAX_VALUE);
        if (savedState != null) {
            mItems = new ArrayList<>();
            mLoadingTasks = savedState.getStringArrayList(STATE_LOADING_TASKS);
        } else {
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

        outState.putStringArrayList(STATE_LOADING_TASKS, mLoadingTasks);
    }

    @Override
    public void loadData() {
        mSubscriptions.add(mIdentityRepo.fetchIdentityDataAsync(mCurrentIdentity)
                .flatMap(new Func1<Identity, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(Identity identity) {
                        return mTaskRepo.getTasksLocalAsync(identity.getGroup(), mDeadlineSelected);
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
                    List<Task> tasksUser;
                    List<Task> tasksGroup;

                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                        tasksUser = new ArrayList<>();
                        tasksGroup = new ArrayList<>();
                    }

                    @Override
                    public void onCompleted() {
                        mItems.add(null);
                        mItems.addAll(tasksUser);
                        mItems.add(null);
                        mItems.addAll(tasksGroup);

                        setLoading(false);
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_tasks_load);
                    }

                    @Override
                    public void onNext(Task task) {
                        final Identity identityResponsible = task.getUserResponsible();
                        if (mCurrentIdentity.getObjectId().equals(identityResponsible.getObjectId())) {
                            tasksUser.add(task);
                        } else {
                            tasksGroup.add(task);
                        }

                        task.setLoading(mLoadingTasks.contains(task.getObjectId()));
                    }
                })
        );
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public void onAddTaskFabClick(View view) {
        mView.startTaskAddActivity();
    }

    @Override
    public void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        int deadline = (int) parent.getItemAtPosition(position);
        if (deadline == R.string.deadline_all) {
            mDeadlineSelected = new Date(Long.MAX_VALUE);
            loadData();
            return;
        }

        Calendar cal = DateUtils.getCalendarInstanceUTC();
        switch (deadline) {
            case R.string.deadline_today: {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            }
            case R.string.deadline_week: {
                int firstDayOfWeek = cal.getFirstDayOfWeek();
                cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            }
            case R.string.deadline_month: {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, 1);
                break;
            }
            case R.string.deadline_year: {
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.add(Calendar.YEAR, 1);
                break;
            }
        }

        cal = DateUtils.resetToMidnight(cal);
        mDeadlineSelected = cal.getTime();
        loadData();
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        setRefreshing(true);
        mView.loadUpdateTasksWorker();
    }

    @Override
    public void setTasksUpdateStream(@NonNull Observable<Task> observable, @NonNull final String workerTag) {
        mSubscriptions.add(observable.toSingle()
                .subscribe(new SingleSubscriber<Task>() {
                    @Override
                    public void onSuccess(Task value) {
                        mView.removeWorker(workerTag);
                        loadData();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessageWithAction(mTaskRepo.getErrorMessage(error),
                                getRefreshAction());
                    }
                })
        );
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
        Task task = mItems.get(position);
        mView.startTaskDetailsActivity(task);
    }

    @Override
    public void onDoneButtonClicked(int position) {
        Task task = mItems.get(position);
        String timeFrame = task.getTimeFrame();

        if (timeFrame.equals(Task.TimeFrame.ONE_TIME)) {
            task.deleteEventually();
            mItems.remove(task);
            mView.notifyItemRemoved(position);
            return;
        }

        task.updateDeadline();
        final Identity identityResponsible = task.getUserResponsible();
        final Identity identityResponsibleNew = task.addHistoryEvent(mCurrentIdentity);
        task.saveEventually();

        String currentUserId = mCurrentUser.getObjectId();
        if (identityResponsible != null && identityResponsible.getObjectId().equals(currentUserId) ||
                identityResponsibleNew != null && identityResponsibleNew.getObjectId().equals(currentUserId)) {
            loadData();
        } else {
            mView.notifyItemChanged(position);
        }
    }

    @Override
    public void onRemindButtonClicked(int position) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        final Task task = mItems.get(position);
        final String taskId = task.getObjectId();
        if (mLoadingTasks.contains(taskId)) {
            return;
        }

        setTaskLoading(task, taskId, position, true);
        mView.loadRemindUserWorker(taskId);
    }

    private void setTaskLoading(@NonNull Task task, String objectId, int position, boolean isLoading) {
        task.setLoading(isLoading);
        mView.notifyItemChanged(position);

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
        mSubscriptions.add(single.subscribe(new SingleSubscriber<String>() {
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
            Task task = mItems.get(i);
            if (task != null && taskId.equals(task.getObjectId())) {
                setTaskLoading(task, taskId, i, false);
                return task;
            }
        }

        return null;
    }
}
