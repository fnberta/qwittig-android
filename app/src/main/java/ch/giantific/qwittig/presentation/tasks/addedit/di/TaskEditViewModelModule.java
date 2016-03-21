/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelEditImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit task screen view model and how to instantiate it.
 */
@Module
public class TaskEditViewModelModule extends BaseViewModelModule<TaskAddEditViewModel.ViewListener> {

    private final String mEditTaskId;

    public TaskEditViewModelModule(@Nullable Bundle savedState,
                                   @NonNull TaskAddEditViewModel.ViewListener view,
                                   @NonNull String editTaskId) {
        super(savedState, view);

        mEditTaskId = editTaskId;
    }

    @PerScreen
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelEditImpl(mSavedState, mView, userRepository, taskRepository,
                mEditTaskId);
    }

}
