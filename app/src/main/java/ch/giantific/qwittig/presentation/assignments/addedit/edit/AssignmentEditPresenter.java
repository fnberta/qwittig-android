/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.edit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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
import ch.giantific.qwittig.presentation.assignments.addedit.add.AssignmentAddPresenter;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items.AssignmentAddEditIdentityItemViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentAddEditContract} interface for the edit task screen.
 */
public class AssignmentEditPresenter extends AssignmentAddPresenter {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";

    private final String assignmentId;
    private Assignment assignment;
    private boolean oldValuesSet;

    public AssignmentEditPresenter(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull UserRepository userRepo,
                                   @NonNull GroupRepository groupRepo,
                                   @NonNull AssignmentRepository assignmentRepo,
                                   @NonNull String assignmentId) {
        super(savedState, navigator, userRepo, groupRepo, assignmentRepo);

        this.assignmentId = assignmentId;

        if (savedState != null) {
            oldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, oldValuesSet);
    }

    @Override
    protected void loadAssignment(@NonNull final FirebaseUser currentUser) {
        subscriptions.add(assignmentRepo.getAssignment(assignmentId)
                .doOnSuccess(editAssignment -> assignment = editAssignment)
                .flatMap(editAssignment -> getInitialChain(currentUser))
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        if (!oldValuesSet) {
                            restoreOldValues(identities);
                            oldValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load edit assignment identities with error:");
                    }
                })
        );
    }

    private void restoreOldValues(@NonNull List<Identity> identities) {
        viewModel.setTitle(assignment.getTitle());
        final String timeFrame = assignment.getTimeFrame();
        handleTimeFrame(timeFrame);
        if (!Objects.equals(timeFrame, TimeFrame.AS_NEEDED)) {
            final Date deadline = assignment.getDeadlineDate();
            viewModel.setDeadline(deadline, dateFormatter.format(deadline));
        }

        final String[] identityIds = assignment.getIdentityIdsSorted();
        for (String identityId : identityIds) {
            for (Iterator<Identity> iterator = identities.iterator(); iterator.hasNext(); ) {
                final Identity identity = iterator.next();
                if (Objects.equals(identityId, identity.getId())) {
                    final AssignmentAddEditIdentityItemViewModel itemViewModel =
                            new AssignmentAddEditIdentityItemViewModel(identity, true);
                    items.add(itemViewModel);
                    iterator.remove();
                    break;
                }
            }
        }

        if (!identities.isEmpty()) {
            for (Identity identity : identities) {
                final AssignmentAddEditIdentityItemViewModel itemViewModel =
                        new AssignmentAddEditIdentityItemViewModel(identity, false);
                items.add(itemViewModel);
            }
        }

        listInteraction.notifyDataSetChanged();
    }

    private void handleTimeFrame(@NonNull @TimeFrame String timeFrame) {
        int res;
        switch (timeFrame) {
            case TimeFrame.ONE_TIME:
                res = R.string.time_frame_one_time;
                break;
            case TimeFrame.DAILY:
                res = R.string.time_frame_daily;
                break;
            case TimeFrame.WEEKLY:
                res = R.string.time_frame_weekly;
                break;
            case TimeFrame.MONTHLY:
                res = R.string.time_frame_monthly;
                break;
            case TimeFrame.YEARLY:
                res = R.string.time_frame_yearly;
                break;
            case TimeFrame.AS_NEEDED:
                res = R.string.time_frame_as_needed;
                break;
            default:
                res = -1;
        }

        if (res != -1) {
            final int selected = Arrays.asList(timeFrames).indexOf(res);
            viewModel.setTimeFrame(res);
            viewModel.setSelectedTimeFrame(selected);
        }
    }

    @Override
    protected boolean changesWereMade() {
        if (!Objects.equals(assignment.getTitle(), viewModel.getTitle()) ||
                !Objects.equals(assignment.getTimeFrame(), getTimeFrameSelected()) ||
                assignment.getDeadlineDate().compareTo(viewModel.getDeadline()) != 0) {
            return true;
        }

        final String[] oldIdentityIds = assignment.getIdentityIdsSorted();
        final List<String> newIdentities = getSelectedIdentityIds();
        if (oldIdentityIds.length != newIdentities.size()) {
            return true;
        }

        for (int i = 0; i < oldIdentityIds.length; i++) {
            final String identityOldId = oldIdentityIds[i];
            final String identityNewId = newIdentities.get(i);
            if (!Objects.equals(identityOldId, identityNewId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void saveAssignment(@NonNull Assignment assignment) {
        assignmentRepo.saveAssignment(assignment, assignmentId);
    }
}
