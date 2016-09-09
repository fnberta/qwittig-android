/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.AssignmentHistoryEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItem;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItemModel;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItemModel.Type;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link AssignmentsViewModel} interface.
 */
public class AssignmentsViewModelImpl extends ListViewModelBaseImpl<AssignmentItemModel, AssignmentsViewModel.ViewListener>
        implements AssignmentsViewModel {

    private static final String STATE_DEADLINE = "STATE_DEADLINE";

    private final AssignmentRepository assignmentRepo;
    private AssignmentDeadline deadline;
    private String currentGroupId;
    private String currentIdentityId;

    public AssignmentsViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepo,
                                    @NonNull AssignmentRepository assignmentRepo,
                                    @NonNull AssignmentDeadline deadline) {
        super(savedState, navigator, eventBus, userRepo);

        this.assignmentRepo = assignmentRepo;

        if (savedState != null) {
            this.deadline = savedState.getParcelable(STATE_DEADLINE);
        } else {
            this.deadline = deadline;
        }
    }

    @Override
    protected Class<AssignmentItemModel> getItemModelClass() {
        return AssignmentItemModel.class;
    }

    @Override
    protected int compareItemModels(AssignmentItemModel o1, AssignmentItemModel o2) {
        if (o1.getViewType() == Type.ASSIGNMENT && o2.getViewType() == Type.ASSIGNMENT) {
            return ((AssignmentItem) o1).compareTo((AssignmentItem) o2);
        }

        return 0;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_DEADLINE, deadline);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
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
        setDataListenerSub(assignmentRepo.observeAssignmentChildren(currentGroupId, currentIdentityId, deadline.getDate())
                .filter(new Func1<RxChildEvent<Assignment>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Assignment> childEvent) {
                        return initialDataLoaded;
                    }
                })
                .flatMap(new Func1<RxChildEvent<Assignment>, Observable<AssignmentItemModel>>() {
                    @Override
                    public Observable<AssignmentItemModel> call(final RxChildEvent<Assignment> event) {
                        return getItemModel(event.getValue(), event.getEventType(), currentIdentityId);
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData() {
        setInitialDataSub(assignmentRepo.getAssignments(currentGroupId, currentIdentityId, deadline.getDate())
                        .flatMap(new Func1<Assignment, Observable<AssignmentItemModel>>() {
                            @Override
                            public Observable<AssignmentItemModel> call(Assignment assignment) {
                                return getItemModel(assignment, EventType.NONE, currentIdentityId);
                            }
                        })
                        .toList()
                        .subscribe(new Subscriber<List<AssignmentItemModel>>() {
                            @Override
                            public void onCompleted() {
                                initialDataLoaded = true;
                                setLoading(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                onDataError(e);
                            }

                            @Override
                            public void onNext(List<AssignmentItemModel> itemModels) {
//                        items.add(new AssignmentHeaderItem(R.string.assignment_header_my, Type.HEADER_MY));
//                        items.add(new AssignmentHeaderItem(R.string.assignment_header_group, Type.HEADER_GROUP));
                                items.addAll(itemModels);
                            }
                        })
        );
    }

    @NonNull
    private Observable<AssignmentItemModel> getItemModel(@NonNull final Assignment assignment,
                                                         final int eventType,
                                                         @NonNull final String identityId) {
        final String[] identityIds = assignment.getIdentityIdsSorted();
        return Observable.from(identityIds)
                .concatMap(new Func1<String, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(String identityId) {
                        return userRepo.getIdentity(identityId).toObservable();
                    }
                })
                .toList()
                .map(new Func1<List<Identity>, AssignmentItemModel>() {
                    @Override
                    public AssignmentItemModel call(List<Identity> identities) {
                        final String upNext = view.buildUpNextString(identities);
                        return new AssignmentItem(eventType, assignment, identities.get(0),
                                getDeadlineText(assignment), upNext, identityId);
                    }
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
    public void onAddAssignmentFabClick(View view) {
        navigator.startAssignmentAdd();
    }

    @Override
    public void onAssignmentDeleted(@NonNull String assignmentId) {
        view.showMessage(R.string.toast_assignment_deleted);
        items.removeItemAt(getPositionForId(assignmentId));
        notifyPropertyChanged(BR.empty);
    }

    @Override
    public void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final AssignmentDeadline deadline = (AssignmentDeadline) parent.getItemAtPosition(position);
        if (!Objects.equals(deadline, this.deadline)) {
            this.deadline = deadline;
            reloadData();
        }
    }

    private void reloadData() {
        items.clear();
        addDataListener();
        loadInitialData();
    }

    @Override
    public void onAssignmentRowClick(@NonNull AssignmentItem itemModel) {
        navigator.startAssignmentDetails(itemModel.getId());
    }

    @Override
    public void onDoneButtonClick(@NonNull AssignmentItem itemModel) {
        final String timeFrame = itemModel.getTimeFrame();
        if (Objects.equals(timeFrame, TimeFrame.ONE_TIME)) {
            assignmentRepo.deleteAssignment(itemModel.getId());
            return;
        }

        final AssignmentHistoryEvent newEvent = new AssignmentHistoryEvent(itemModel.getId(),
                currentIdentityId, new Date());
        assignmentRepo.addHistoryEvent(newEvent, itemModel.getIdentitiesSorted(),
                itemModel.getTimeFrame());
    }

    @Override
    public void onRemindButtonClick(@NonNull AssignmentItem itemModel) {
        final String nickname = itemModel.getNickname();
        if (itemModel.isPending()) {
            view.showMessage(R.string.toast_remind_pending, nickname);
        } else {
            assignmentRepo.remindResponsible(itemModel.getId());
            view.showMessage(R.string.toast_assignment_reminded_user, nickname);
        }
    }
}
