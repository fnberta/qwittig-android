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
    final ArrayList<TaskAddEditIdentityItemModel> mItems;
    final TaskRepository mTaskRepo;
    private final int[] mTimeFrames = new int[]{
            R.string.time_frame_daily,
            R.string.time_frame_weekly,
            R.string.time_frame_monthly,
            R.string.time_frame_yearly,
            R.string.time_frame_as_needed,
            R.string.time_frame_one_time};
    private final DateFormat mDateFormatter;
    Date mDeadline;
    String mTitle;
    ListInteraction mListInteraction;
    private Identity mCurrentIdentity;
    private ListDragInteraction mListDragInteraction;
    private int mTimeFrame;

    public TaskAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                       @NonNull Navigator navigator,
                                       @NonNull RxBus<Object> eventBus,
                                       @NonNull UserRepository userRepository,
                                       @NonNull TaskRepository taskRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mTaskRepo = taskRepository;

        if (savedState != null) {
            mTitle = savedState.getString(STATE_TITLE);
            mDeadline = new Date(savedState.getLong(STATE_DEADLINE_SELECTED));
            mItems = savedState.getParcelableArrayList(STATE_ITEMS);
        } else {
            mItems = new ArrayList<>();
            mDeadline = new Date();
        }

        mDateFormatter = DateUtils.getDateFormatter(false);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putString(STATE_TITLE, mTitle);
        outState.putLong(STATE_DEADLINE_SELECTED, mDeadline.getTime());
        outState.putParcelableArrayList(STATE_ITEMS, mItems);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        mListInteraction = listInteraction;
    }

    @Override
    public void setListDragInteraction(@NonNull ListDragInteraction listDragInteraction) {
        mListDragInteraction = listDragInteraction;
    }

    @Override
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public TaskAddEditIdentityItemModel getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        throw new RuntimeException("Only one view type supported.");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int[] getTimeFrames() {
        return mTimeFrames;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);


    }

//    @Override
//    public void loadData() {
//        getSubscriptions().add(mUserRepo.getGroupIdentities(mCurrentIdentity.getGroup(), true)
//                .toList()
//                .toSingle()
//                .subscribe(new SingleSubscriber<List<Identity>>() {
//                    @Override
//                    public void onSuccess(List<Identity> identities) {
//                        mItems.clear();
//
//                        final int size = identities.size();
//                        if (mTaskIdentities.isEmpty()) {
//                            for (int i = 0; i < size; i++) {
//                                final Identity identity = identities.get(i);
//                                mItems.add(identity);
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
//                            Collections.addAll(mItems, userArray);
//                            for (Identity identity : identities) {
//                                mItems.add(identity);
//                                mTaskIdentities.add(new TaskUser(identity.getObjectId(), false));
//                            }
//                        }
//
//                        mListInteraction.notifyDataSetChanged();
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
        return mTitle;
    }

    public void setTitle(@NonNull String title) {
        mTitle = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDeadline() {
        return mDateFormatter.format(mDeadline);
    }

    public void setDeadline(@NonNull Date deadline) {
        mDeadline = deadline;
        notifyPropertyChanged(BR.deadline);
    }

    @Override
    @Bindable
    public boolean isAsNeededTask() {
        return mTimeFrame == R.string.time_frame_as_needed;
    }

    @Override
    @Bindable
    public int getSelectedTimeFrame() {
        return Arrays.asList(mTimeFrames).indexOf(mTimeFrame);
    }

    @Override
    public void setTimeFrame(int timeFrame) {
        mTimeFrame = timeFrame;
        notifyPropertyChanged(BR.selectedTimeFrame);
    }

    @Override
    public void onUpOrBackClick() {
        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mNavigator.finish(Activity.RESULT_CANCELED);
        }
    }

    boolean changesWereMade() {
        return !TextUtils.isEmpty(mTitle);
    }

    @Override
    public void onTitleChanged(CharSequence s, int start, int before, int count) {
        mTitle = s.toString();
    }

    @Override
    public void onDeadlineClicked(View view) {
        mView.showDatePickerDialog();
    }

    @Override
    public void onFabSaveTaskClick(View view) {
        if (TextUtils.isEmpty(mTitle)) {
            mView.showMessage(R.string.error_task_title);
            return;
        }

        final String timeFrame = getTimeFrameSelected();
        final List<String> identities = getIdentitiesAvailable();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME) && identities.size() > 1) {
            mView.showMessage(R.string.toast_task_max_one_user_one_time);
            return;
        }

        final Task task = getTask(mTitle, timeFrame, identities);
        mTaskRepo.saveTask(task);
        mNavigator.finish(TaskAddEditViewModel.TaskResult.TASK_SAVED);
    }

    @TimeFrame
    final String getTimeFrameSelected() {
        switch (mTimeFrame) {
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
                mDeadline = null;
                return TimeFrame.AS_NEEDED;
        }
    }

    @NonNull
    final List<String> getIdentitiesAvailable() {
        final List<String> identities = new ArrayList<>();

        for (TaskAddEditIdentityItemModel itemModel : mItems) {
            if (itemModel.isInvolved()) {
                identities.add(itemModel.getIdentityId());
            }
        }

        return identities;
    }

    @NonNull
    Task getTask(@NonNull String taskTitle, @NonNull String timeFrame,
                 @NonNull List<String> identities) {
        return new Task(mCurrentIdentity.getId(), taskTitle, mCurrentIdentity.getGroup(), timeFrame,
                mDeadline, identities);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        mTimeFrame = (int) parent.getItemAtPosition(position);
        notifyPropertyChanged(BR.asNeededTask);
    }

    @Override
    public void onUsersRowItemClick(@NonNull TaskAddEditIdentityItemModel itemModel) {
        final int pos = mItems.indexOf(itemModel);
        if (itemModel.isInvolved()) {
            if (!userIsLastOneChecked()) {
                itemModel.setInvolved(false);
                mListInteraction.notifyItemChanged(pos);
            } else {
                mView.showMessage(R.string.toast_min_one_user);
            }
        } else {
            itemModel.setInvolved(true);
            mListInteraction.notifyItemChanged(pos);
        }
    }

    private boolean userIsLastOneChecked() {
        int involvedCount = 0;
        for (TaskAddEditIdentityItemModel itemModel : mItems) {
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
        mListDragInteraction.startDrag(viewHolder);
    }

    @Override
    public void onDiscardChangesSelected() {
        mNavigator.finish(TaskAddEditViewModel.TaskResult.TASK_DISCARDED);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        mListInteraction.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        mListInteraction.notifyItemRemoved(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        setDeadline(deadline);
    }
}
