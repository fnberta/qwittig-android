/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditViewModel;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditViewModelAddImpl;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the add task screen view model and how to instantiate it.
 */
@Module
public class AssignmentAddViewModelModule extends BaseViewModelModule {

    public AssignmentAddViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    AssignmentAddEditViewModel providesAssignmentAddEditViewModel(@NonNull Navigator navigator,
                                                            @NonNull RxBus<Object> eventBus,
                                                            @NonNull UserRepository userRepository,
                                                            @NonNull GroupRepository groupRepository,
                                                            @NonNull AssignmentRepository assignmentRepository) {
        return new AssignmentAddEditViewModelAddImpl(savedState, navigator, eventBus, userRepository,
                groupRepository, assignmentRepository);
    }

}
