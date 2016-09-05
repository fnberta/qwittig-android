/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

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
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.assignments.addedit.itemmodels.AssignmentAddEditIdentityItemModel;
import ch.giantific.qwittig.presentation.common.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentAddEditViewModel} interface for the add task screen.
 */
public class AssignmentAddEditViewModelAddImpl extends ViewModelBaseImpl<AssignmentAddEditViewModel.ViewListener>
        implements AssignmentAddEditViewModel {

    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_ITEMS = "STATE_ITEMS";
    private static final String STATE_TITLE = "STATE_TITLE";

    final ArrayList<AssignmentAddEditIdentityItemModel> items;
    final AssignmentRepository assignmentRepo;
    private final GroupRepository groupRepo;
    private final int[] timeFrames = new int[]{
            R.string.time_frame_daily,
            R.string.time_frame_weekly,
            R.string.time_frame_monthly,
            R.string.time_frame_yearly,
            R.string.time_frame_as_needed,
            R.string.time_frame_one_time};
    private final DateFormat dateFormatter;
    List<Identity> groupIdentities;
    Date deadline;
    String title;
    ListInteraction listInteraction;
    private String currentGroupId;
    private ListDragInteraction listDragInteraction;
    private int timeFrame;

    public AssignmentAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                             @NonNull Navigator navigator,
                                             @NonNull RxBus<Object> eventBus,
                                             @NonNull UserRepository userRepo,
                                             @NonNull GroupRepository groupRepo, @NonNull AssignmentRepository assignmentRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.assignmentRepo = assignmentRepo;
        this.groupRepo = groupRepo;

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
    public AssignmentAddEditIdentityItemModel getItemAtPosition(int position) {
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

        loadAssignment(currentUser);
    }

    void loadAssignment(@NonNull FirebaseUser currentUser) {
        getSubscriptions().add(getInitialChain(currentUser)
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        if (items.isEmpty()) {
                            addInitialIdentityRows(identities);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load assignment identities with error:");
                    }
                })
        );
    }

    @NonNull
    final Single<List<Identity>> getInitialChain(@NonNull FirebaseUser currentUser) {
        return userRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(final User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        currentGroupId = identity.getGroup();
                    }
                })
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return groupRepo.getGroupIdentities(identity.getGroup(), true);
                    }
                })
                .toSortedList()
                .toSingle();
    }

    private void addInitialIdentityRows(@NonNull List<Identity> identities) {
        for (int i = 0, size = identities.size(); i < size; i++) {
            items.add(new AssignmentAddEditIdentityItemModel(identities.get(i), true));
            listInteraction.notifyItemInserted(i);
        }
    }

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
    public boolean isAsNeeded() {
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
    public void onDeadlineClicked(View view) {
        this.view.showDatePickerDialog();
    }

    @Override
    public void onFabSaveAssignmentClick(View view) {
        if (TextUtils.isEmpty(title)) {
            this.view.showMessage(R.string.error_assignment_title);
            return;
        }

        final String timeFrame = getTimeFrameSelected();
        final List<String> identities = getSelectedIdentityIds();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME) && identities.size() > 1) {
            this.view.showMessage(R.string.toast_assignment_max_one_user_one_time);
            return;
        }

        final Assignment assignment = new Assignment(title, currentGroupId, timeFrame, deadline, identities);
        saveAssignment(assignment);
        navigator.finish(AssignmentResult.SAVED);
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
    final List<String> getSelectedIdentityIds() {
        final List<String> identityIds = new ArrayList<>();
        for (AssignmentAddEditIdentityItemModel itemModel : items) {
            if (itemModel.isSelected()) {
                identityIds.add(itemModel.getIdentityId());
            }
        }

        return identityIds;
    }

    void saveAssignment(@NonNull Assignment assignment) {
        assignmentRepo.saveAssignment(assignment, null);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        timeFrame = (int) parent.getItemAtPosition(position);
        notifyPropertyChanged(BR.asNeeded);
    }

    @Override
    public void onIdentitiesRowItemClick(@NonNull AssignmentAddEditIdentityItemModel itemModel) {
        final int pos = items.indexOf(itemModel);
        if (itemModel.isSelected()) {
            if (!identityIsLastOneChecked()) {
                itemModel.setSelected(false);
                listInteraction.notifyItemChanged(pos);
            } else {
                view.showMessage(R.string.toast_min_one_user);
            }
        } else {
            itemModel.setSelected(true);
            listInteraction.notifyItemChanged(pos);
        }
    }

    private boolean identityIsLastOneChecked() {
        int involvedCount = 0;
        for (AssignmentAddEditIdentityItemModel itemModel : items) {
            if (itemModel.isSelected()) {
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
        navigator.finish(AssignmentResult.DISCARDED);
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
