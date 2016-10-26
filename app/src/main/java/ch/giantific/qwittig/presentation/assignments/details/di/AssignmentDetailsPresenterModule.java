/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.di;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsContract;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsPresenter;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.AssignmentDetailsViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task details screen view model and how to
 * instantiate it.
 */
@Module
public class AssignmentDetailsPresenterModule {

    private final String assignmentId;

    public AssignmentDetailsPresenterModule(@NonNull String assignmentId) {
        this.assignmentId = assignmentId;
    }

    @PerActivity
    @Provides
    AssignmentDetailsContract.Presenter providesAssignmentDetailsPresenter(@NonNull Navigator navigator,
                                                                           @NonNull AssignmentDetailsViewModel viewModel,
                                                                           @NonNull UserRepository userRepo,
                                                                           @NonNull AssignmentRepository assignmentRepo) {
        return new AssignmentDetailsPresenter(navigator, viewModel, userRepo, assignmentRepo, assignmentId);
    }

}
