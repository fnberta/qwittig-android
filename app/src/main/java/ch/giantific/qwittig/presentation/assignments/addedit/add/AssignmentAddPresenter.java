/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.add;

import android.app.Activity;
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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditContract;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditContract.AssignmentResult;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.AssignmentAddEditViewModel;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items.AssignmentAddEditIdentityItemViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentAddEditContract} interface for the add task screen.
 */
public class AssignmentAddPresenter extends BasePresenterImpl<AssignmentAddEditContract.ViewListener>
        implements AssignmentAddEditContract.Presenter {

    protected static final String STATE_VIEW_MODEL = AssignmentAddEditViewModel.class.getCanonicalName();
    private static final String STATE_ITEMS = "STATE_ITEMS";
    protected final AssignmentAddEditViewModel viewModel;
    protected final ArrayList<AssignmentAddEditIdentityItemViewModel> items;
    protected final AssignmentRepository assignmentRepo;
    protected final DateFormat dateFormatter;
    protected final int[] timeFrames = new int[]{
            R.string.time_frame_daily,
            R.string.time_frame_weekly,
            R.string.time_frame_monthly,
            R.string.time_frame_yearly,
            R.string.time_frame_as_needed,
            R.string.time_frame_one_time};
    private final GroupRepository groupRepo;
    protected ListInteraction listInteraction;
    private String currentGroupId;
    private ListDragInteraction listDragInteraction;

    public AssignmentAddPresenter(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull UserRepository userRepo,
                                  @NonNull GroupRepository groupRepo, @NonNull AssignmentRepository assignmentRepo) {
        super(savedState, navigator, userRepo);

        this.assignmentRepo = assignmentRepo;
        this.groupRepo = groupRepo;
        dateFormatter = DateUtils.getDateFormatter(false);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
            items = savedState.getParcelableArrayList(STATE_ITEMS);
        } else {
            final Date date = new Date();
            viewModel = new AssignmentAddEditViewModel(date, dateFormatter.format(date));
            items = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
        outState.putParcelableArrayList(STATE_ITEMS, items);
    }

    @Override
    public AssignmentAddEditViewModel getViewModel() {
        return viewModel;
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
    public int[] getTimeFrames() {
        return timeFrames;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        loadAssignment(currentUser);
    }

    protected void loadAssignment(@NonNull FirebaseUser currentUser) {
        subscriptions.add(getInitialChain(currentUser)
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
    protected final Single<List<Identity>> getInitialChain(@NonNull FirebaseUser currentUser) {
        return userRepo.getUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> currentGroupId = identity.getGroup())
                .flatMapObservable(identity -> groupRepo.getGroupIdentities(identity.getGroup(), true))
                .toSortedList()
                .toSingle();
    }

    private void addInitialIdentityRows(@NonNull List<Identity> identities) {
        for (int i = 0, size = identities.size(); i < size; i++) {
            items.add(new AssignmentAddEditIdentityItemViewModel(identities.get(i), true));
            listInteraction.notifyItemInserted(i);
        }
    }

    @Override
    public AssignmentAddEditIdentityItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onUpOrBackClick() {
        if (changesWereMade()) {
            view.showDiscardChangesDialog();
        } else {
            navigator.finish(Activity.RESULT_CANCELED);
        }
    }

    protected boolean changesWereMade() {
        return !TextUtils.isEmpty(viewModel.getTitle());
    }

    @Override
    public void onDeadlineClicked(View view) {
        this.view.showDatePickerDialog();
    }

    @Override
    public void onSaveAssignmentClick(View view) {
        final String title = viewModel.getTitle();
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

        final Date deadline = !Objects.equals(timeFrame, TimeFrame.AS_NEEDED) ? viewModel.getDeadline() : null;
        final Assignment assignment = new Assignment(title, currentGroupId, timeFrame, deadline, identities);
        saveAssignment(assignment);
        navigator.finish(AssignmentResult.SAVED);
    }

    @TimeFrame
    protected final String getTimeFrameSelected() {
        switch (viewModel.getTimeFrame()) {
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
                return TimeFrame.AS_NEEDED;
        }
    }

    @NonNull
    protected final List<String> getSelectedIdentityIds() {
        final List<String> identityIds = new ArrayList<>();
        for (AssignmentAddEditIdentityItemViewModel itemViewModel : items) {
            if (itemViewModel.isSelected()) {
                identityIds.add(itemViewModel.getIdentityId());
            }
        }

        return identityIds;
    }

    protected void saveAssignment(@NonNull Assignment assignment) {
        assignmentRepo.saveAssignment(assignment, null);
    }

    @Override
    public void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final int timeFrame = (int) parent.getItemAtPosition(position);
        viewModel.setTimeFrame(timeFrame);
    }

    @Override
    public void onIdentitiesRowItemClick(@NonNull AssignmentAddEditIdentityItemViewModel itemViewModel) {
        final int pos = items.indexOf(itemViewModel);
        if (itemViewModel.isSelected()) {
            if (!identityIsLastOneChecked()) {
                itemViewModel.setSelected(false);
                listInteraction.notifyItemChanged(pos);
            } else {
                view.showMessage(R.string.toast_min_one_user);
            }
        } else {
            itemViewModel.setSelected(true);
            listInteraction.notifyItemChanged(pos);
        }
    }

    private boolean identityIsLastOneChecked() {
        int involvedCount = 0;
        for (AssignmentAddEditIdentityItemViewModel itemViewModel : items) {
            if (itemViewModel.isSelected()) {
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
        viewModel.setDeadline(deadline, dateFormatter.format(deadline));
    }
}
