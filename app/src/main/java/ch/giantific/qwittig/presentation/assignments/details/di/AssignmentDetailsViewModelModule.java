/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.AssignmentRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsViewModel;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task details screen view model and how to
 * instantiate it.
 */
@Module
public class AssignmentDetailsViewModelModule extends BaseViewModelModule {

    private final String assignmentId;

    public AssignmentDetailsViewModelModule(@Nullable Bundle savedState,
                                            @NonNull String assignmentId) {
        super(savedState);

        this.assignmentId = assignmentId;
    }

    @PerActivity
    @Provides
    AssignmentDetailsViewModel providesAssignmentDetailsViewModel(@NonNull Navigator navigator,
                                                            @NonNull RxBus<Object> eventBus,
                                                            @NonNull UserRepository userRepository,
                                                            @NonNull AssignmentRepository assignmentRepository) {
        return new AssignmentDetailsViewModelImpl(savedState, navigator, eventBus, userRepository,
                assignmentRepository, assignmentId);
    }

}
