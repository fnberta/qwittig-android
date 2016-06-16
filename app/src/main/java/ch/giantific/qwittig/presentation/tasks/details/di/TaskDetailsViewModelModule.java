/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModel;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task details screen view model and how to
 * instantiate it.
 */
@Module
public class TaskDetailsViewModelModule extends BaseViewModelModule<TaskDetailsViewModel.ViewListener> {

    private final String mTaskId;

    public TaskDetailsViewModelModule(@Nullable Bundle savedState,
                                      @NonNull TaskDetailsViewModel.ViewListener view,
                                      @NonNull String taskId) {
        super(savedState, view);

        mTaskId = taskId;
    }

    @PerScreen
    @Provides
    TaskDetailsViewModel providesTaskDetailsViewModel(@NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskDetailsViewModelImpl(mSavedState, mView, eventBus, userRepository, taskRepository, mTaskId);
    }

}
