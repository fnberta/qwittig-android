/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModel;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class TasksListViewModelModule extends BaseViewModelModule<TasksViewModel.ViewListener> {

    public TasksListViewModelModule(@Nullable Bundle savedState,
                                    @NonNull TasksViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    TasksViewModel providesTasksListViewModel(@NonNull UserRepository userRepository,
                                              @NonNull IdentityRepository identityRepository,
                                              @NonNull TaskRepository taskRepository) {
        return new TasksViewModelImpl(mSavedState, mView, identityRepository, userRepository, taskRepository);
    }

}
