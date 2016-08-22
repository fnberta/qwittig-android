/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.Task.TimeFrame;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsHistoryItem;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsItemModel;

/**
 * Provides an implementation of the {@link TaskDetailsViewModel} interface.
 */
public class TaskDetailsViewModelImpl extends ListViewModelBaseImpl<TaskDetailsItemModel, TaskDetailsViewModel.ViewListener>
        implements TaskDetailsViewModel {

    private final String taskId;
    private final TaskRepository taskRepo;
    private String title;
    @StringRes
    private int timeFrame;
    private SpannableStringBuilder identitiesText;
    private String currentIdentityId;
    private boolean currentUserResponsible;

    public TaskDetailsViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull TaskRepository taskRepository,
                                    @NonNull String taskId) {
        super(savedState, navigator, eventBus, userRepository);

        taskRepo = taskRepository;
        this.taskId = taskId;
    }

    @Override
    protected Class<TaskDetailsItemModel> getItemModelClass() {
        return TaskDetailsItemModel.class;
    }

    @Override
    protected int compareItemModels(TaskDetailsItemModel o1, TaskDetailsItemModel o2) {
        if (o1 instanceof TaskDetailsHistoryItem) {
            if (o2 instanceof TaskDetailsHistoryItem) {
                return ((TaskDetailsHistoryItem) o1).compareTo((TaskDetailsHistoryItem) o2);
            }
        }

        return 0;
    }

    @Override
    @Bindable
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(@NonNull String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @StringRes
    @Bindable
    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(@StringRes int timeFrame) {
        this.timeFrame = timeFrame;
        notifyPropertyChanged(BR.timeFrame);
    }

    @Bindable
    public SpannableStringBuilder getIdentitiesText() {
        return identitiesText;
    }

    public void setIdentitiesText(@NonNull SpannableStringBuilder identitiesText) {
        this.identitiesText = identitiesText;
        notifyPropertyChanged(BR.identitiesText);
    }

    @Override
    @Bindable
    public boolean isCurrentUserResponsible() {
        return currentUserResponsible;
    }

    @Override
    public void setCurrentUserResponsible(boolean currentUserResponsible) {
        this.currentUserResponsible = currentUserResponsible;
        notifyPropertyChanged(BR.currentUserResponsible);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        setLoading(false);
        view.startPostponedEnterTransition();
        view.showMessage(R.string.toast_error_task_details_load);
    }

    //    @Override
//    public void loadData() {
//        getSubscriptions().add(taskRepo.getTask(taskId)
//                .flatMapObservable(new Func1<Task, Observable<TaskHistoryEvent>>() {
//                    @Override
//                    public Observable<TaskHistoryEvent
//                            > call(Task task) {
//                        mTask = task;
//
//                        updateToolbarHeader();
//                        updateToolbarMenu();
//
//                        return taskRepo.getTaskHistoryEvents(task);
//                    }
//                })
//                .map(new Func1<TaskHistoryEvent, TaskDetailsHistoryItem>() {
//                    @Override
//                    public TaskDetailsHistoryItem call(TaskHistoryEvent taskHistoryEvent) {
//                        return new TaskDetailsHistoryItem(taskHistoryEvent);
//                    }
//                })
//                .subscribe(new Subscriber<TaskDetailsHistoryItem>() {
//                    @Override
//                    public void onStart() {
//                        super.onStart();
//                        items.clear();
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        Collections.sort(items, Collections.reverseOrder());
//                        items.add(0, new TaskDetailsHeaderItem(R.string.header_task_history));
//                        listInteraction.notifyDataSetChanged();
//                        setLoading(false);
//                        view.startPostponedEnterTransition();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setLoading(false);
//                        view.startPostponedEnterTransition();
//                        view.showMessage(R.string.toast_error_task_details_load);
//                    }
//
//                    @Override
//                    public void onNext(TaskDetailsHistoryItem taskHistoryItem) {
//                        items.add(taskHistoryItem);
//                    }
//                })
//        );
//    }

    private void updateToolbarHeader(@NonNull Task task) {
        setTitle(task.getTitle());
        updateTimeFrame(task.getTimeFrame());
//        updateIdentities();
    }

    private void updateTimeFrame(@NonNull String timeFrame) {
        int timeFrameLocalized;
        switch (timeFrame) {
            case TimeFrame.DAILY:
                timeFrameLocalized = R.string.time_frame_daily;
                break;
            case TimeFrame.WEEKLY:
                timeFrameLocalized = R.string.time_frame_weekly;
                break;
            case TimeFrame.MONTHLY:
                timeFrameLocalized = R.string.time_frame_monthly;
                break;
            case TimeFrame.YEARLY:
                timeFrameLocalized = R.string.time_frame_yearly;
                break;
            case TimeFrame.AS_NEEDED:
                timeFrameLocalized = R.string.time_frame_as_needed;
                break;
            case TimeFrame.ONE_TIME:
                timeFrameLocalized = R.string.time_frame_one_time;
                break;
            default:
                timeFrameLocalized = -1;
        }
        if (timeFrameLocalized != -1) {
            setTimeFrame(timeFrameLocalized);
        }
    }

    private void updateIdentities(@NonNull Task task, @NonNull List<Identity> identities) {
        final String identityResponsible = task.getIdentityIdResponsible();
        setCurrentUserResponsible(Objects.equals(currentIdentityId, identityResponsible));

        final SpannableStringBuilder stringBuilder = view.buildTaskIdentitiesString(identities,
                identityResponsible);
        setIdentitiesText(stringBuilder);
    }

    private void updateToolbarMenu(@NonNull String taskInitiator) {
        boolean showEditOptions = Objects.equals(taskInitiator, currentIdentityId);

//        if (showEditOptions) {
//            final List<Identity> identities = mTask.getIdentities();
//            for (Identity identity : identities) {
//                if (!identity.isActive()) {
//                    showEditOptions = false;
//                    break;
//                }
//            }
//        }

        view.toggleEditOptions(showEditOptions);
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            if (items.get(i).getViewType() == TaskDetailsItemModel.Type.HISTORY) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onDeleteTaskMenuClick() {
        taskRepo.deleteTask(taskId);
        navigator.finish(TaskDetailsResult.TASK_DELETED);
    }

    @Override
    public void onEditTaskMenuClick() {
        navigator.startTaskEdit(taskId);
    }

    @Override
    public void onFabDoneClick(View view) {
//        final String timeFrame = mTask.getTimeFrame();
//        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
//            taskRepo.deleteTask(taskId);
//            navigator.finish(TaskDetailsResult.TASK_DELETED);
//            return;
//        }
//
//        final TaskHistoryEvent2 newEvent = new TaskHistoryEvent2(taskId, currentIdentityId, new Date());
//        taskRepo.addHistoryEvent(newEvent);
//        updateToolbarHeader();
    }

//    @SuppressLint("MissingSuperCall")
//    @Override
//    protected void onIdentitySelected(@NonNull Identity identitySelected) {
//        navigator.finish(TaskDetailsResult.GROUP_CHANGED);
//    }
}
