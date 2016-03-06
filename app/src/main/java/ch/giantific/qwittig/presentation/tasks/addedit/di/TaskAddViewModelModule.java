/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModelAddImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class TaskAddViewModelModule extends BaseViewModelModule<TaskAddEditViewModel.ViewListener> {

    public TaskAddViewModelModule(@Nullable Bundle savedState,
                                  @NonNull TaskAddEditViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    TaskAddEditViewModel providesTaskAddEditViewModel(@NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskAddEditViewModelAddImpl(mSavedState, mView, userRepository, taskRepository);
    }

}
