/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelAddImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the add task screen view model and how to instantiate it.
 */
@Module
public class TaskAddViewModelModule extends BaseViewModelModule {

    public TaskAddViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull Navigator navigator,
                                                      @NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelAddImpl(mSavedState, navigator, eventBus, userRepository,
                taskRepository);
    }

}
