/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsViewModelImpl;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task list screen view model and how to
 * instantiate it.
 */
@Module
public class AssignmentsViewModelModule extends BaseViewModelModule {

    private final AssignmentDeadline assignmentDeadline;

    public AssignmentsViewModelModule(@Nullable Bundle savedState,
                                      @NonNull AssignmentDeadline assignmentDeadline) {
        super(savedState);

        this.assignmentDeadline = assignmentDeadline;
    }

    @PerActivity
    @Provides
    AssignmentsViewModel providesAssignmentsViewModel(@NonNull Navigator navigator,
                                                    @NonNull RxBus<Object> eventBus,
                                                    @NonNull UserRepository userRepository,
                                                    @NonNull AssignmentRepository assignmentRepository) {
        return new AssignmentsViewModelImpl(savedState, navigator, eventBus, userRepository,
                assignmentRepository, assignmentDeadline);
    }

}
