/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsContract;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsPresenter;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task list screen view model and how to
 * instantiate it.
 */
@Module
public class AssignmentsPresenterModule extends BasePresenterModule {

    private final AssignmentDeadline assignmentDeadline;

    public AssignmentsPresenterModule(@Nullable Bundle savedState,
                                      @NonNull AssignmentDeadline assignmentDeadline) {
        super(savedState);

        this.assignmentDeadline = assignmentDeadline;
    }

    @PerActivity
    @Provides
    AssignmentsContract.Presenter providesAssignmentsPresenter(@NonNull Navigator navigator,
                                                               @NonNull UserRepository userRepo,
                                                               @NonNull AssignmentRepository assignmentRepo) {
        return new AssignmentsPresenter(savedState, navigator, userRepo, assignmentRepo, assignmentDeadline);
    }

}
