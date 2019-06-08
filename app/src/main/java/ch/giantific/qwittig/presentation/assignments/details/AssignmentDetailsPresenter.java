/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.AssignmentHistory;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsContract.AssignmentDetailsResult;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.AssignmentDetailsViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHeaderItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHistoryItemViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.Observable;

/**
 * Provides an implementation of the {@link AssignmentDetailsContract} interface.
 */
public class AssignmentDetailsPresenter extends BasePresenterImpl<AssignmentDetailsContract.ViewListener>
        implements AssignmentDetailsContract.Presenter {

    private final AssignmentDetailsViewModel viewModel;
    private final String assignmentId;
    private final AssignmentRepository assignmentRepo;
    private Assignment assignment;
    private String currentIdentityId;
    private boolean identitiesActive = true;

    @Inject
    public AssignmentDetailsPresenter(@NonNull Navigator navigator,
                                      @NonNull AssignmentDetailsViewModel viewModel,
                                      @NonNull UserRepository userRepo,
                                      @NonNull AssignmentRepository assignmentRepo,
                                      @NonNull String assignmentId) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.assignmentRepo = assignmentRepo;
        this.assignmentId = assignmentId;
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .doOnNext(currentIdentityId -> {
                    if (!TextUtils.isEmpty(this.currentIdentityId) && !Objects.equals(this.currentIdentityId, currentIdentityId)) {
                        navigator.finish();
                    }
                    this.currentIdentityId = currentIdentityId;
                })
                .flatMap(currentIdentityId -> assignmentRepo.observeAssignment(assignmentId))
                .doOnNext(assignment -> {
                    this.assignment = assignment;
                    updateToolbarHeader();
                    view.clearItems();
                    view.addItem(new AssignmentDetailsHeaderItemViewModel(R.string.header_assignment_history));
                })
                .flatMap(assignment -> Observable.from(assignment.getIdentityIdsSorted())
                        .concatMap(identityId -> userRepo.getIdentity(identityId).toObservable())
                        .toList())
                .doOnNext(identities -> {
                    updateIdentities(identities);
                    checkIdentitiesActive(identities);
                })
                .flatMap(identities -> assignmentRepo.getAssignmentHistory(assignmentId)
                        .map(assignmentHistory -> {
                            final String identityId = assignmentHistory.getIdentity();
                            for (Identity identity : identities) {
                                if (Objects.equals(identityId, identity.getId())) {
                                    return new AssignmentDetailsHistoryItemViewModel(assignmentHistory, identity);
                                }
                            }

                            // should never happen
                            return null;
                        })
                        .toSortedList())
                .subscribe(new IndefiniteSubscriber<List<AssignmentDetailsHistoryItemViewModel>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        viewModel.setLoading(false);
                        view.startPostponedEnterTransition();
                        view.showMessage(R.string.toast_error_assignment_details_load);
                    }

                    @Override
                    public void onNext(List<AssignmentDetailsHistoryItemViewModel> itemViewModels) {
                        view.clearItems();
                        if (!itemViewModels.isEmpty()) {
                            view.addItem(new AssignmentDetailsHeaderItemViewModel(R.string.header_assignment_history));
                            view.addItems(itemViewModels);
                            viewModel.setEmpty(false);
                        }
                        view.notifyItemsChanged();
                        viewModel.setLoading(false);
                        view.startPostponedEnterTransition();
                    }
                })
        );
    }

    private void updateToolbarHeader() {
        viewModel.setTitle(assignment.getTitle());
        updateTimeFrame(assignment.getTimeFrame());
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
            viewModel.setTimeFrame(timeFrameLocalized);
        }
    }

    private void updateIdentities(@NonNull List<Identity> identities) {
        viewModel.setResponsible(Objects.equals(currentIdentityId, assignment.getIdentityIdResponsible()));
        viewModel.setIdentitiesText(view.buildIdentitiesString(identities));
    }

    private void checkIdentitiesActive(@NonNull List<Identity> identities) {
        for (Identity identity : identities) {
            if (!identity.isActive()) {
                identitiesActive = false;
                break;
            }
        }
    }

    @Override
    public void onDeleteAssignmentMenuClick() {
        if (identitiesActive) {
            assignmentRepo.deleteAssignment(assignmentId);
            navigator.finish(AssignmentDetailsResult.DELETED, assignmentId);
        } else {
            view.showMessage(R.string.toast_assignment_edit_identities_inactive);
        }
    }

    @Override
    public void onEditAssignmentMenuClick() {
        if (identitiesActive) {
            navigator.startAssignmentEdit(assignmentId);
        } else {
            view.showMessage(R.string.toast_assignment_edit_identities_inactive);
        }
    }

    @Override
    public void onDoneClick(View view) {
        final String timeFrame = assignment.getTimeFrame();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
            assignmentRepo.deleteAssignment(assignmentId);
            navigator.finish(AssignmentDetailsResult.DELETED);
            return;
        }

        final AssignmentHistory assignmentHistory = new AssignmentHistory(assignmentId,
                currentIdentityId, new Date());
        assignmentRepo.addHistory(assignmentHistory, assignment.getIdentityIdsSorted(),
                assignment.getTimeFrame());
    }
}
