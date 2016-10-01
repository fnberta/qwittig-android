/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditContract;
import ch.giantific.qwittig.presentation.assignments.addedit.edit.AssignmentEditPresenter;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit task screen view model and how to instantiate it.
 */
@Module
public class AssignmentEditPresenterModule extends BasePresenterModule {

    private final String assignmentId;

    public AssignmentEditPresenterModule(@Nullable Bundle savedState,
                                         @NonNull String assignmentId) {
        super(savedState);

        this.assignmentId = assignmentId;
    }

    @PerActivity
    @Provides
    AssignmentAddEditContract.Presenter providesAssignmentEditPresenter(@NonNull Navigator navigator,
                                                                        @NonNull UserRepository userRepo,
                                                                        @NonNull GroupRepository groupRepo,
                                                                        @NonNull AssignmentRepository assignmentRepo) {
        return new AssignmentEditPresenter(savedState, navigator, userRepo, groupRepo,
                assignmentRepo, assignmentId);
    }

}
