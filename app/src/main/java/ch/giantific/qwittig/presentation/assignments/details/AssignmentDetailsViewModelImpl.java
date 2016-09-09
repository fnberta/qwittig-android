/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.AssignmentHistoryEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsHeaderItem;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsHistoryItem;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link AssignmentDetailsViewModel} interface.
 */
public class AssignmentDetailsViewModelImpl extends ViewModelBaseImpl<AssignmentDetailsViewModel.ViewListener>
        implements AssignmentDetailsViewModel {

    private final List<AssignmentDetailsItemModel> items = new ArrayList<>();
    private final String assignmentId;
    private final AssignmentRepository assignmentRepo;
    private ListInteraction listInteraction;
    private Assignment assignment;
    private String currentIdentityId;
    private String title;
    @StringRes
    private int timeFrame;
    private SpannableStringBuilder identitiesText;
    private boolean responsible;
    private boolean identitiesActive = true;

    public AssignmentDetailsViewModelImpl(@Nullable Bundle savedState,
                                          @NonNull Navigator navigator,
                                          @NonNull RxBus<Object> eventBus,
                                          @NonNull UserRepository userRepository,
                                          @NonNull AssignmentRepository assignmentRepo,
                                          @NonNull String assignmentId) {
        super(savedState, navigator, eventBus, userRepository);

        this.assignmentRepo = assignmentRepo;
        this.assignmentId = assignmentId;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
    }

    @Override
    public AssignmentDetailsItemModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAtPosition(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            if (items.get(i).getViewType() == AssignmentDetailsItemModel.Type.HISTORY) {
                return false;
            }
        }

        return true;
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

    @Bindable
    public boolean isResponsible() {
        return responsible;
    }

    public void setResponsible(boolean responsible) {
        this.responsible = responsible;
        notifyPropertyChanged(BR.responsible);
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        final String identityId = user.getCurrentIdentity();
                        if (!TextUtils.isEmpty(currentIdentityId)
                                && !Objects.equals(currentIdentityId, identityId)) {
                            navigator.finish();
                        }

                        currentIdentityId = identityId;
                    }
                })
                .flatMap(new Func1<User, Observable<Assignment>>() {
                    @Override
                    public Observable<Assignment> call(User user) {
                        return assignmentRepo.observeAssignment(assignmentId);
                    }
                })
                .doOnNext(new Action1<Assignment>() {
                    @Override
                    public void call(Assignment assignmentObj) {
                        assignment = assignmentObj;

                        updateToolbarHeader();
                        items.clear();
                        items.add(new AssignmentDetailsHeaderItem(R.string.header_assignment_history));
                    }
                })
                .flatMap(new Func1<Assignment, Observable<List<Identity>>>() {
                    @Override
                    public Observable<List<Identity>> call(Assignment assignment) {
                        return Observable.from(assignment.getIdentityIdsSorted())
                                .concatMap(new Func1<String, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(String identityId) {
                                        return userRepo.getIdentity(identityId).toObservable();
                                    }
                                })
                                .toList();
                    }
                })
                .doOnNext(new Action1<List<Identity>>() {
                    @Override
                    public void call(List<Identity> identities) {
                        updateIdentities(identities);
                        checkIdentitiesActive(identities);
                    }
                })
                .flatMap(new Func1<List<Identity>, Observable<List<AssignmentDetailsHistoryItem>>>() {
                    @Override
                    public Observable<List<AssignmentDetailsHistoryItem>> call(final List<Identity> identities) {
                        return assignmentRepo.getAssignmentHistoryEvents(assignmentId)
                                .map(new Func1<AssignmentHistoryEvent, AssignmentDetailsHistoryItem>() {
                                    @Override
                                    public AssignmentDetailsHistoryItem call(AssignmentHistoryEvent historyEvent) {
                                        final String identityId = historyEvent.getIdentity();
                                        for (Identity identity : identities) {
                                            if (Objects.equals(identityId, identity.getId())) {
                                                return new AssignmentDetailsHistoryItem(historyEvent, identity);
                                            }
                                        }

                                        return null;
                                    }
                                })
                                .toSortedList();
                    }
                })
                .subscribe(new IndefiniteSubscriber<List<AssignmentDetailsHistoryItem>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        setLoading(false);
                        view.startPostponedEnterTransition();
                        view.showMessage(R.string.toast_error_assignment_details_load);
                    }

                    @Override
                    public void onNext(List<AssignmentDetailsHistoryItem> itemModels) {
                        items.clear();
                        if (!itemModels.isEmpty()) {
                            items.add(new AssignmentDetailsHeaderItem(R.string.header_assignment_history));
                            items.addAll(itemModels);
                        }
                        listInteraction.notifyDataSetChanged();
                        setLoading(false);
                        view.startPostponedEnterTransition();
                    }
                })
        );
    }

    private void updateToolbarHeader() {
        setTitle(assignment.getTitle());
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
            setTimeFrame(timeFrameLocalized);
        }
    }

    private void updateIdentities(@NonNull List<Identity> identities) {
        setResponsible(Objects.equals(currentIdentityId, assignment.getIdentityIdResponsible()));
        setIdentitiesText(view.buildIdentitiesString(identities));
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
    public void onFabDoneClick(View view) {
        final String timeFrame = assignment.getTimeFrame();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
            assignmentRepo.deleteAssignment(assignmentId);
            navigator.finish(AssignmentDetailsResult.DELETED);
            return;
        }

        final AssignmentHistoryEvent historyEvent = new AssignmentHistoryEvent(assignmentId,
                currentIdentityId, new Date());
        assignmentRepo.addHistoryEvent(historyEvent, assignment.getIdentityIdsSorted(),
                assignment.getTimeFrame());
    }
}
