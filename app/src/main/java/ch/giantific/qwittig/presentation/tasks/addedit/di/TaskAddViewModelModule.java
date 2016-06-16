/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelAddImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the add task screen view model and how to instantiate it.
 */
@Module
public class TaskAddViewModelModule extends BaseViewModelModule<TaskAddEditViewModel.ViewListener> {

    public TaskAddViewModelModule(@Nullable Bundle savedState,
                                  @NonNull TaskAddEditViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelAddImpl(mSavedState, mView, eventBus, userRepository, taskRepository);
    }

}
