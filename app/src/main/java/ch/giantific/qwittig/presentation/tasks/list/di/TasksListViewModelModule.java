/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModel;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModelImpl;
import ch.giantific.qwittig.presentation.tasks.list.models.TaskDeadline;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the task list screen view model and how to
 * instantiate it.
 */
@Module
public class TasksListViewModelModule extends BaseViewModelModule {

    private final TaskDeadline mTaskDeadline;

    public TasksListViewModelModule(@Nullable Bundle savedState,
                                    @NonNull TaskDeadline taskDeadline) {
        super(savedState);

        mTaskDeadline = taskDeadline;
    }

    @PerActivity
    @Provides
    TasksViewModel providesTasksListViewModel(@NonNull Navigator navigator,
                                              @NonNull RxBus<Object> eventBus,
                                              @NonNull UserRepository userRepository,
                                              @NonNull TaskRepository taskRepository) {
        return new TasksViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                taskRepository, mTaskDeadline);
    }

}
