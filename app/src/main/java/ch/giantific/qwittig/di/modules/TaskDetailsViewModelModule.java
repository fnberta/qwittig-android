/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModel;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class TaskDetailsViewModelModule extends BaseViewModelModule<TaskDetailsViewModel.ViewListener> {

    String mTaskId;

    public TaskDetailsViewModelModule(@Nullable Bundle savedState,
                                      @NonNull TaskDetailsViewModel.ViewListener view,
                                      @NonNull String taskId) {
        super(savedState, view);

        mTaskId = taskId;
    }

    @PerFragment
    @Provides
    TaskDetailsViewModel providesTaskDetailsViewModel(@NonNull UserRepository userRepository,
                                                      @NonNull IdentityRepository identityRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskDetailsViewModelImpl(mSavedState, mView, userRepository, identityRepository,
                taskRepository, mTaskId);
    }

}
