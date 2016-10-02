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
import ch.giantific.qwittig.presentation.assignments.addedit.add.AssignmentAddPresenter;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the add task screen view model and how to instantiate it.
 */
@Module
public class AssignmentAddPresenterModule extends BasePresenterModule {

    public AssignmentAddPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    AssignmentAddEditContract.Presenter providesAssignmentAddPresenter(@NonNull Navigator navigator,
                                                                       @NonNull UserRepository userRepo,
                                                                       @NonNull GroupRepository groupRepo,
                                                                       @NonNull AssignmentRepository assignmentRepo) {
        return new AssignmentAddPresenter(savedState, navigator, userRepo, groupRepo, assignmentRepo);
    }

}
