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

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import ch.giantific.qwittig.presentation.common.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.addedit.itemmodels.TaskAddEditIdentityItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides an implementation of the {@link TaskAddEditViewModel} interface for the add task screen.
 */
public class TaskAddEditViewModelAddImpl extends ViewModelBaseImpl<TaskAddEditViewModel.ViewListener>
        implements TaskAddEditViewModel {

    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_ITEMS = "STATE_ITEMS";
    private static final String STATE_TITLE = "STATE_TITLE";

    final ArrayList<TaskAddEditIdentityItemModel> items;
    final TaskRepository taskRepo;
    private final int[] timeFrames = new int[]{
            R.string.time_frame_daily,
            R.string.time_frame_weekly,
            R.string.time_frame_monthly,
            R.string.time_frame_yearly,
            R.string.time_frame_as_needed,
            R.string.time_frame_one_time};
    private final DateFormat dateFormatter;
    Date deadline;
    String title;
    ListInteraction listInteraction;
    private Identity currentIdentity;
    private ListDragInteraction listDragInteraction;
    private int timeFrame;

    public TaskAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                       @NonNull Navigator navigator,
                                       @NonNull RxBus<Object> eventBus,
                                       @NonNull UserRepository userRepo,
                                       @NonNull TaskRepository taskRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.taskRepo = taskRepo;

        if (savedState != null) {
            title = savedState.getString(STATE_TITLE);
            deadline = new Date(savedState.getLong(STATE_DEADLINE_SELECTED));
            items = savedState.getParcelableArrayList(STATE_ITEMS);
        } else {
            items = new ArrayList<>();
            deadline = new Date();
        }

        dateFormatter = DateUtils.getDateFormatter(false);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putString(STATE_TITLE, title);
        outState.putLong(STATE_DEADLINE_SELECTED, deadline.getTime());
        outState.putParcelableArrayList(STATE_ITEMS, items);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
    }

    @Override
    public void setListDragInteraction(@NonNull ListDragInteraction listDragInteraction) {
        this.listDragInteraction = listDragInteraction;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public TaskAddEditIdentityItemModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        throw new RuntimeException("Only one view type supported.");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int[] getTimeFrames() {
        return timeFrames;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);


    }

//    @Override
//    public void loadData() {
//        getSubscriptions().add(userRepo.getGroupIdentities(currentIdentity.getGroup(), true)
//                .toList()
//                .toSingle()
//                .subscribe(new SingleSubscriber<List<Identity>>() {
//                    @Override
//                    public void onSuccess(List<Identity> identities) {
//                        items.clear();
//
//                        final int size = identities.size();
//                        if (mTaskIdentities.isEmpty()) {
//                            for (int i = 0; i < size; i++) {
//                                final Identity identity = identities.get(i);
//                                items.add(identity);
//                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), true));
//                            }
//                        } else {
//                            final int sizeInvolved = mTaskIdentities.size();
//                            final Identity[] userArray = new Identity[sizeInvolved];
//                            final List<String> ids = new ArrayList<>(sizeInvolved);
//                            for (int i = 0; i < sizeInvolved; i++) {
//                                final TaskUser taskUser = mTaskIdentities.get(i);
//                                ids.add(taskUser.getIdentityId());
//                            }
//
//                            for (Iterator<Identity> iterator = identities.iterator(); iterator.hasNext(); ) {
//                                final Identity identity = iterator.next();
//                                final String userId = identity.getObjectId();
//                                if (ids.contains(userId)) {
//                                    final int pos = ids.indexOf(userId);
//                                    userArray[pos] = identity;
//                                    iterator.remove();
//                                }
//                            }
//
//                            Collections.addAll(items, userArray);
//                            for (Identity identity : identities) {
//                                items.add(identity);
//                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), false));
//                            }
//                        }
//
//                        listInteraction.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onError(Throwable error) {
//                        // TODO:handle error
//                    }
//                })
//        );
//    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDeadline() {
        return dateFormatter.format(deadline);
    }

    public void setDeadline(@NonNull Date deadline) {
        this.deadline = deadline;
        notifyPropertyChanged(BR.deadline);
    }

    @Override
    @Bindable
    public boolean isAsNeededTask() {
        return timeFrame == R.string.time_frame_as_needed;
    }

    @Override
    @Bindable
    public int getSelectedTimeFrame() {
        return Arrays.asList(timeFrames).indexOf(timeFrame);
    }

    @Override
    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
        notifyPropertyChanged(BR.selectedTimeFrame);
    }

    @Override
    public void onUpOrBackClick() {
        if (changesWereMade()) {
            view.showDiscardChangesDialog();
        } else {
            navigator.finish(Activity.RESULT_CANCELED);
        }
    }

    boolean changesWereMade() {
        return !TextUtils.isEmpty(title);
    }

    @Override
    public void onTitleChanged(CharSequence s, int start, int before, int count) {
        title = s.toString();
    }

    @Override
    public void onDeadlineClicked(View view) {
        this.view.showDatePickerDialog();
    }

    @Override
    public void onFabSaveTaskClick(View view) {
        if (TextUtils.isEmpty(title)) {
            this.view.showMessage(R.string.error_task_title);
            return;
        }

        final String timeFrame = getTimeFrameSelected();
        final List<String> identities = getIdentitiesAvailable();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME) && identities.size() > 1) {
            this.view.showMessage(R.string.toast_task_max_one_user_one_time);
            return;
        }

        final Task task = getTask(title, timeFrame, identities);
        taskRepo.saveTask(task);
        navigator.finish(TaskAddEditViewModel.TaskResult.TASK_SAVED);
    }

    @TimeFrame
    final String getTimeFrameSelected() {
        switch (timeFrame) {
            case R.string.time_frame_daily:
                return TimeFrame.DAILY;
            case R.string.time_frame_weekly:
                return TimeFrame.WEEKLY;
            case R.string.time_frame_monthly:
                return TimeFrame.MONTHLY;
            case R.string.time_frame_yearly:
                return TimeFrame.YEARLY;
            case R.string.time_frame_one_time:
                return TimeFrame.ONE_TIME;
            default:
                deadline = null;
                return TimeFrame.AS_NEEDED;
        }
    }

    @NonNull
    final List<String> getIdentitiesAvailable() {
        final List<String> identities = new ArrayList<>();

        for (TaskAddEditIdentityItemModel itemModel : items) {
            if (itemModel.isInvolved()) {
                identities.add(itemModel.getIdentityId());
            }
        }

        return identities;
    }

    @NonNull
    Task getTask(@NonNull String taskTitle, @NonNull String timeFrame,
                 @NonNull List<String> identities) {
        return new Task(currentIdentity.getId(), taskTitle, currentIdentity.getGroup(), timeFrame,
                deadline, identities);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        timeFrame = (int) parent.getItemAtPosition(position);
        notifyPropertyChanged(BR.asNeededTask);
    }

    @Override
    public void onUsersRowItemClick(@NonNull TaskAddEditIdentityItemModel itemModel) {
        final int pos = items.indexOf(itemModel);
        if (itemModel.isInvolved()) {
            if (!userIsLastOneChecked()) {
                itemModel.setInvolved(false);
                listInteraction.notifyItemChanged(pos);
            } else {
                view.showMessage(R.string.toast_min_one_user);
            }
        } else {
            itemModel.setInvolved(true);
            listInteraction.notifyItemChanged(pos);
        }
    }

    private boolean userIsLastOneChecked() {
        int involvedCount = 0;
        for (TaskAddEditIdentityItemModel itemModel : items) {
            if (itemModel.isInvolved()) {
                involvedCount++;
            }

            if (involvedCount > 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        listDragInteraction.startDrag(viewHolder);
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(TaskAddEditViewModel.TaskResult.TASK_DISCARDED);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        listInteraction.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        items.remove(position);
        listInteraction.notifyItemRemoved(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        setDeadline(deadline);
    }
}
