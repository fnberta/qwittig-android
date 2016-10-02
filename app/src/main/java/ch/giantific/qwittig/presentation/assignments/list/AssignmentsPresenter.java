/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
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
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link AssignmentsContract} interface.
 */
public class AssignmentsPresenter extends BasePresenterImpl<AssignmentsContract.ViewListener>
        implements AssignmentsContract.Presenter {

    private static final String STATE_VIEW_MODEL = AssignmentsViewModel.class.getCanonicalName();
    private final AssignmentsViewModel viewModel;
    private final SortedList<BaseAssignmentItemViewModel> items;
    private final SortedListCallback<BaseAssignmentItemViewModel> listCallback;
    private final ChildEventSubscriber<BaseAssignmentItemViewModel, AssignmentsViewModel> subscriber;
    private final AssignmentRepository assignmentRepo;
    private boolean initialDataLoaded;
    private String currentGroupId;
    private String currentIdentityId;

    public AssignmentsPresenter(@Nullable Bundle savedState,
                                @NonNull Navigator navigator,
                                @NonNull UserRepository userRepo,
                                @NonNull AssignmentRepository assignmentRepo,
                                @NonNull AssignmentDeadline deadline) {
        super(savedState, navigator, userRepo);

        this.assignmentRepo = assignmentRepo;

        listCallback = new SortedListCallback<BaseAssignmentItemViewModel>() {
            @Override
            public int compare(BaseAssignmentItemViewModel o1, BaseAssignmentItemViewModel o2) {
                if (o1.getViewType() == ViewType.ASSIGNMENT && o2.getViewType() == ViewType.ASSIGNMENT) {
                    return ((AssignmentItemViewModel) o1).compareTo((AssignmentItemViewModel) o2);
                }

                return 0;
            }
        };
        items = new SortedList<>(BaseAssignmentItemViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new AssignmentsViewModel(true, deadline);
        }

        //noinspection ConstantConditions
        subscriber = new ChildEventSubscriber<>(items, viewModel, e ->
                view.showMessage(R.string.toast_error_assignments_load));
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public AssignmentsViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        initialDataLoaded = false;
                        currentIdentityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            items.clear();
                        }
                        currentGroupId = groupId;
                        addDataListener();
                        loadInitialData();
                    }
                })
        );
    }

    private void addDataListener() {
        subscriptions.add(assignmentRepo.observeAssignmentChildren(currentGroupId, currentIdentityId, viewModel.getDeadline().getDate())
                .filter(childEvent -> initialDataLoaded)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), currentIdentityId))
                .subscribe(subscriber)
        );
    }

    private void loadInitialData() {
        subscriptions.add(assignmentRepo.getAssignments(currentGroupId, currentIdentityId, viewModel.getDeadline().getDate())
                        .takeWhile(assignment -> Objects.equals(assignment.getGroup(), currentGroupId))
                        .flatMap(assignment -> getItemViewModel(assignment, EventType.NONE, currentIdentityId))
                        .toList()
                        .subscribe(new Subscriber<List<BaseAssignmentItemViewModel>>() {
                            @Override
                            public void onCompleted() {
                                initialDataLoaded = true;
                                viewModel.setEmpty(getItemCount() == 0);
                                viewModel.setLoading(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "failed to load initial assignments with error:");
                                view.showMessage(R.string.toast_error_assignments_load);
                            }

                            @Override
                            public void onNext(List<BaseAssignmentItemViewModel> itemViewModels) {
//                        items.add(new AssignmentHeaderItem(R.string.assignment_header_my, Type.HEADER_MY));
//                        items.add(new AssignmentHeaderItem(R.string.assignment_header_group, Type.HEADER_GROUP));
                                items.addAll(itemViewModels);
                            }
                        })
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
    public BaseAssignmentItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAddAssignmentClick(View view) {
        navigator.startAssignmentAdd();
    }

    @Override
    public void onAssignmentDeleted(@NonNull String assignmentId) {
        view.showMessage(R.string.toast_assignment_deleted);
        items.removeItemAt(subscriber.getPositionForId(assignmentId));
        viewModel.setEmpty(getItemCount() == 0);
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
        items.clear();
        viewModel.setEmpty(true);
        viewModel.setLoading(true);
        addDataListener();
        loadInitialData();
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
