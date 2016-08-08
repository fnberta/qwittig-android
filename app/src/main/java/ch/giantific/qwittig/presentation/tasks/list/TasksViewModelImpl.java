/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItem;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItemModel;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItemModel.Type;
import ch.giantific.qwittig.presentation.tasks.list.models.TaskDeadline;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link TasksViewModel} interface.
 */
public class TasksViewModelImpl extends ListViewModelBaseImpl<TasksItemModel, TasksViewModel.ViewListener>
        implements TasksViewModel {

    private static final String STATE_LOADING_TASKS = "STATE_LOADING_TASKS";
    private static final String STATE_DEADLINE = "STATE_DEADLINE";
    private final TaskRepository mTaskRepo;
    private final ArrayList<String> mLoadingTasks;
    private TaskDeadline mDeadline;
    private String mCurrentIdentityId;

    public TasksViewModelImpl(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull RxBus<Object> eventBus,
                              @NonNull UserRepository userRepository,
                              @NonNull TaskRepository taskRepository,
                              @NonNull TaskDeadline deadline) {
        super(savedState, navigator, eventBus, userRepository);

        mTaskRepo = taskRepository;

        if (savedState != null) {
            mDeadline = savedState.getParcelable(STATE_DEADLINE);
            mLoadingTasks = savedState.getStringArrayList(STATE_LOADING_TASKS);
        } else {
            mDeadline = deadline;
            mLoadingTasks = new ArrayList<>();
        }
    }

    @Override
    protected Class<TasksItemModel> getItemModelClass() {
        return TasksItemModel.class;
    }

    @Override
    protected int compareItemModels(TasksItemModel o1, TasksItemModel o2) {
        if (o1 instanceof TasksItem) {
            if (o2 instanceof TasksItem) {
                return ((TasksItem) o1).compareTo((TasksItem) o2);
            }
        }

        return 0;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_DEADLINE, mDeadline);
        outState.putStringArrayList(STATE_LOADING_TASKS, mLoadingTasks);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

    }

    //    @Override
//    public void loadData() {
//        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
//                .flatMapObservable(new Func1<Identity, Observable<Task>>() {
//                    @Override
//                    public Observable<Task> call(Identity identity) {
//                        return mTaskRepo.getTasks(identity, mDeadline.getDate());
//                    }
//                })
//                .filter(new Func1<Task, Boolean>() {
//                    @Override
//                    public Boolean call(Task task) {
//                        final List<Identity> identities = task.getIdentitiesText();
//                        return !identities.isEmpty() && identities.contains(mCurrentIdentity);
//                    }
//                })
//                .subscribe(new Subscriber<Task>() {
//                    List<TasksItemModel> tasksUser;
//                    List<TasksItemModel> tasksGroup;
//
//                    @Override
//                    public void onStart() {
//                        super.onStart();
//                        mItems.clear();
//                        tasksUser = new ArrayList<>();
//                        tasksGroup = new ArrayList<>();
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        if (!tasksUser.isEmpty()) {
//                            mItems.add(new TasksHeaderItem(R.string.task_header_my));
//                            mItems.addAll(tasksUser);
//                        }
//                        if (!tasksGroup.isEmpty()) {
//                            mItems.add(new TasksHeaderItem(R.string.task_header_group));
//                            mItems.addAll(tasksGroup);
//                        }
//
//                        setLoading(false);
//                        mListInteraction.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        mView.showMessage(R.string.toast_error_tasks_load);
//                    }
//
//                    @Override
//                    public void onNext(Task task) {
//                        final Identity identityResponsible = task.getIdentityResponsible();
//                        if (Objects.equals(mCurrentIdentity.getObjectId(), identityResponsible.getObjectId())) {
//                            tasksUser.add(new TasksItem(task, mCurrentIdentity));
//                        } else {
//                            tasksGroup.add(new TasksItem(task, mCurrentIdentity));
//                        }
//
//                        task.setLoading(mLoadingTasks.contains(task.getObjectId()));
//                    }
//                })
//        );
//    }

    @Override
    public void onAddTaskFabClick(View view) {
        mNavigator.startTaskAdd();
    }

    @Override
    public void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final TaskDeadline deadline = (TaskDeadline) parent.getItemAtPosition(position);
        if (!Objects.equals(deadline, mDeadline)) {
            mDeadline = deadline;
            // TODO: reload data
        }
    }

    @Override
    public void onTaskRowClick(@NonNull TasksItem itemModel) {
        mNavigator.startTaskDetails(itemModel.getId());
    }

    @Override
    public void onDoneButtonClick(@NonNull TasksItem itemModel) {
        final String timeFrame = itemModel.getTimeFrame();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
            mTaskRepo.deleteTask(itemModel.getId());
            return;
        }

        final TaskHistoryEvent newEvent = new TaskHistoryEvent(itemModel.getId(), mCurrentIdentityId, new Date());
        mTaskRepo.addHistoryEvent(newEvent);

        // TODO: rotate identities and get new responsible

//        if (identityResponsible != null && Objects.equals(identityResponsible, mCurrentIdentityId) ||
//                identityResponsibleNew != null && Objects.equals(identityResponsibleNew, mCurrentIdentityId)) {
//            loadData();
//        } else {
//            final int pos = mItems.indexOf(itemModel);
//            mListInteraction.notifyItemChanged(pos);
//        }
    }

    @Override
    public void onRemindButtonClick(@NonNull TasksItem itemModel) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        if (itemModel.isItemLoading()) {
            return;
        }

        final int pos = mItems.indexOf(itemModel);
        setTaskLoading(itemModel, pos, true);
        mView.loadRemindUserWorker(itemModel.getId());
    }

    private void setTaskLoading(@NonNull TasksItem itemModel, int position, boolean isLoading) {
        itemModel.setItemLoading(isLoading);
        mListInteraction.notifyItemChanged(position);

        final String id = itemModel.getId();
        if (isLoading) {
            mLoadingTasks.add(id);
        } else {
            mLoadingTasks.remove(id);
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
                        final TasksItem itemModel = stopTaskLoading(taskId);
                        if (itemModel != null) {
                            final Identity identityResponsible = itemModel.getIdentities().get(0);
                            final String nickname = identityResponsible.getNickname();
                            mView.showMessage(R.string.toast_task_reminded_user, nickname);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessage(R.string.toast_error_remind_failed);
                        stopTaskLoading(taskId);
                    }
                })
        );
    }

    @Nullable
    private TasksItem stopTaskLoading(@NonNull String taskId) {
        for (int i = 0, tasksSize = mItems.size(); i < tasksSize; i++) {
            final TasksItemModel taskListItem = mItems.get(i);
            if (taskListItem.getViewType() != Type.TASK) {
                continue;
            }

            final TasksItem itemModel = (TasksItem) taskListItem;
            if (Objects.equals(taskId, itemModel.getId())) {
                setTaskLoading(itemModel, i, false);
                return itemModel;
            }
        }

        return null;
    }
}
