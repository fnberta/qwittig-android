/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsContract;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsPresenter;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task details screen view model and how to
 * instantiate it.
 */
@Module
public class AssignmentDetailsPresenterModule extends BasePresenterModule {

    private final String assignmentId;

    public AssignmentDetailsPresenterModule(@Nullable Bundle savedState,
                                            @NonNull String assignmentId) {
        super(savedState);

        this.assignmentId = assignmentId;
    }

    @PerActivity
    @Provides
    AssignmentDetailsContract.Presenter providesAssignmentDetailsPresenter(@NonNull Navigator navigator,
                                                                           @NonNull UserRepository userRepo,
                                                                           @NonNull AssignmentRepository assignmentRepo) {
        return new AssignmentDetailsPresenter(savedState, navigator, userRepo, assignmentRepo, assignmentId);
    }

}
