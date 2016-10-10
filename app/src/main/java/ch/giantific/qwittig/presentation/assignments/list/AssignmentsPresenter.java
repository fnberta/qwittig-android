/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

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
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.AssignmentsViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.AssignmentItemViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.BaseAssignmentItemViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.BaseAssignmentItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;

/**
 * Provides an implementation of the {@link AssignmentsContract} interface.
 */
public class AssignmentsPresenter extends BasePresenterImpl<AssignmentsContract.ViewListener>
        implements AssignmentsContract.Presenter {

    private static final AssignmentDeadline[] DEADLINES = new AssignmentDeadline[]{
            AssignmentDeadline.newAllInstance(R.string.deadline_all),
            AssignmentDeadline.newTodayInstance(R.string.deadline_today),
            AssignmentDeadline.newWeekInstance(R.string.deadline_week),
            AssignmentDeadline.newMonthInstance(R.string.deadline_month),
            AssignmentDeadline.newYearInstance(R.string.deadline_year)
    };
    private final AssignmentsViewModel viewModel;
    private final AssignmentRepository assignmentRepo;
    private String currentGroupId;
    private String currentIdentityId;

    @Inject
    public AssignmentsPresenter(@NonNull Navigator navigator,
                                @NonNull AssignmentsViewModel viewModel,
                                @NonNull UserRepository userRepo,
                                @NonNull AssignmentRepository assignmentRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.assignmentRepo = assignmentRepo;

        if (this.viewModel.getDeadline() == null) {
            this.viewModel.setDeadline(DEADLINES[0]);
        }
    }

    @Override
    public int compareItemViewModels(@NonNull BaseAssignmentItemViewModel item1, @NonNull BaseAssignmentItemViewModel item2) {
        if (item1.getViewType() == ViewType.ASSIGNMENT && item2.getViewType() == ViewType.ASSIGNMENT) {
            return ((AssignmentItemViewModel) item1).compareTo((AssignmentItemViewModel) item2);
        }

        return 0;
    }

    @Override
    public AssignmentDeadline[] getAssignmentDeadlines() {
        return DEADLINES;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        currentIdentityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            view.clearItems();
                        }
                        currentGroupId = groupId;
                        addDataListener();
                    }
                })
        );
    }

    private void addDataListener() {
        final Observable<List<BaseAssignmentItemViewModel>> initialData = assignmentRepo.getAssignments(currentGroupId, currentIdentityId, viewModel.getDeadline().getDate())
                .flatMap(assignment -> getItemViewModel(assignment, EventType.NONE, currentIdentityId))
                .toList()
                .doOnNext(itemViewModels -> {
//                    items.addItemAtPosition(new AssignmentHeaderItem(R.string.assignment_header_my, Type.HEADER_MY));
//                    items.addItemAtPosition(new AssignmentHeaderItem(R.string.assignment_header_group, Type.HEADER_GROUP));
                    view.addItems(itemViewModels);
                    viewModel.setEmpty(view.isItemsEmpty());
                    viewModel.setLoading(false);
                });
        subscriptions.add(assignmentRepo.observeAssignmentChildren(currentGroupId, currentIdentityId, viewModel.getDeadline().getDate())
                .skipUntil(initialData)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), currentIdentityId))
                .subscribe(new ChildEventSubscriber<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_assignments_load)))
        );
    }

    @NonNull
    private Observable<BaseAssignmentItemViewModel> getItemViewModel(@NonNull final Assignment assignment,
                                                                     final int eventType,
                                                                     @NonNull final String currentIdentityId) {
        final String[] identityIds = assignment.getIdentityIdsSorted();
        return Observable.from(identityIds)
                .concatMap(identityId -> userRepo.getIdentity(identityId).toObservable())
                .toList()
                .map(identities -> {
                    final String upNext = view.buildUpNextString(identities);
                    return new AssignmentItemViewModel(eventType, assignment, identities.get(0),
                            getDeadlineText(assignment), upNext, currentIdentityId);
                });
    }

    private String getDeadlineText(@NonNull Assignment assignment) {
        if (Objects.equals(assignment.getTimeFrame(), TimeFrame.AS_NEEDED)) {
            return view.buildDeadlineString(R.string.time_frame_as_needed);
        }

        final int daysToDeadline = assignment.getDaysToDeadline();
        if (daysToDeadline == 0) {
            return view.buildDeadlineString(R.string.deadline_today);
        } else if (daysToDeadline == -1) {
            return view.buildDeadlineString(R.string.yesterday);
        } else if (daysToDeadline < 0) {
            return view.buildDeadlineString(R.string.deadline_text_neg, daysToDeadline * -1);
        } else {
            return view.buildDeadlineString(R.string.deadline_text_pos, daysToDeadline);
        }
    }

    @Override
    public void onAddAssignmentClick(View view) {
        navigator.startAssignmentAdd();
    }

    @Override
    public void onAssignmentDeleted(@NonNull String assignmentId) {
        view.showMessage(R.string.toast_assignment_deleted);
        view.removeItemAtPosition(view.getItemPositionForId(assignmentId));
        viewModel.setEmpty(view.isItemsEmpty());
    }

    @Override
    public void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final AssignmentDeadline deadline = (AssignmentDeadline) parent.getItemAtPosition(position);
        if (!Objects.equals(deadline, viewModel.getDeadline())) {
            viewModel.setDeadline(deadline);
            reloadData();
        }
    }

    private void reloadData() {
        view.clearItems();
        viewModel.setEmpty(true);
        viewModel.setLoading(true);
        addDataListener();
    }

    @Override
    public void onAssignmentRowClick(@NonNull AssignmentItemViewModel itemViewModel) {
        navigator.startAssignmentDetails(itemViewModel.getId());
    }

    @Override
    public void onDoneButtonClick(@NonNull AssignmentItemViewModel itemViewModel) {
        final String timeFrame = itemViewModel.getTimeFrame();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
            assignmentRepo.deleteAssignment(itemViewModel.getId());
            return;
        }

        final AssignmentHistory newEvent = new AssignmentHistory(itemViewModel.getId(),
                currentIdentityId, new Date());
        assignmentRepo.addHistory(newEvent, itemViewModel.getIdentitiesSorted(),
                itemViewModel.getTimeFrame());
    }

    @Override
    public void onRemindButtonClick(@NonNull AssignmentItemViewModel itemViewModel) {
        final String nickname = itemViewModel.getNickname();
        if (itemViewModel.isPending()) {
            view.showMessage(R.string.toast_remind_pending, nickname);
        } else {
            assignmentRepo.remindResponsible(itemViewModel.getId());
            view.showMessage(R.string.toast_assignment_reminded_user, nickname);
        }
    }
}
