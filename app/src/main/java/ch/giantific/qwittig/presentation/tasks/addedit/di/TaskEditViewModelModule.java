/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelEditImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
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

    @PerFragment
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull UserRepository userRepository,
                                                      @NonNull IdentityRepository identityRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelEditImpl(mSavedState, mView, userRepository, identityRepository,
                taskRepository, mEditTaskId);
    }

}
