/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.add;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

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
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentAddEditContract} interface for the addItemAtPosition task screen.
 */
public class AssignmentAddPresenter extends BasePresenterImpl<AssignmentAddEditContract.ViewListener>
        implements AssignmentAddEditContract.Presenter {

    protected final AssignmentAddEditViewModel viewModel;
    protected final AssignmentRepository assignmentRepo;
    protected final DateFormat dateFormatter;
    private final GroupRepository groupRepo;
    private String currentGroupId;

    @Inject
    public AssignmentAddPresenter(@NonNull Navigator navigator,
                                  @NonNull AssignmentAddEditViewModel viewModel,
                                  @NonNull UserRepository userRepo,
                                  @NonNull GroupRepository groupRepo, @NonNull AssignmentRepository assignmentRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.assignmentRepo = assignmentRepo;
        this.groupRepo = groupRepo;

        dateFormatter = DateUtils.getDateFormatter(false);
        if (TextUtils.isEmpty(viewModel.getDeadlineFormatted())) {
            final Date date = new Date();
            viewModel.setDeadline(date, dateFormatter.format(date));
        }
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
                        if (view.isIdentitiesEmpty()) {
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
            view.addIdentity(new AssignmentAddEditIdentityItemViewModel(identities.get(i), true));
        }
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
        for (AssignmentAddEditIdentityItemViewModel itemViewModel : viewModel.getIdentities()) {
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
        if (viewModel.getTimeFrame() != timeFrame) {
            viewModel.setTimeFrame(timeFrame, false);
        }
    }

    @Override
    public void onIdentitiesRowItemClick(@NonNull AssignmentAddEditIdentityItemViewModel itemViewModel) {
        if (itemViewModel.isSelected()) {
            if (!identityIsLastOneChecked()) {
                itemViewModel.setSelected(false);
                view.notifyIdentityChanged(itemViewModel);
            } else {
                view.showMessage(R.string.toast_min_one_user);
            }
        } else {
            itemViewModel.setSelected(true);
            view.notifyIdentityChanged(itemViewModel);
        }
    }

    private boolean identityIsLastOneChecked() {
        int involvedCount = 0;
        for (AssignmentAddEditIdentityItemViewModel itemViewModel : viewModel.getIdentities()) {
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
        view.startDragIdentity(viewHolder);
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(AssignmentResult.DISCARDED);
    }

    @Override
    public void onIdentityMove(int fromPosition, int toPosition) {
        view.swapIdentity(fromPosition, toPosition);
    }

    @Override
    public void onIdentityDismiss(int position) {
        view.removeIdentityAtPosition(position);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        viewModel.setDeadline(deadline, dateFormatter.format(deadline));
    }
}
