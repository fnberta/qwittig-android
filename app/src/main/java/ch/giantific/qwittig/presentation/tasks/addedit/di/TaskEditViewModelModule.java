/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelEditImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit task screen view model and how to instantiate it.
 */
@Module
public class TaskEditViewModelModule extends BaseViewModelModule {

    private final String mEditTaskId;

    public TaskEditViewModelModule(@Nullable Bundle savedState,
                                   @NonNull String editTaskId) {
        super(savedState);

        mEditTaskId = editTaskId;
    }

    @PerActivity
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull Navigator navigator,
                                                      @NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelEditImpl(mSavedState, navigator, eventBus, userRepository,
                taskRepository, mEditTaskId);
    }

}
