/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class TaskDetailsViewModelModule extends BaseViewModelModule {

    String mTaskId;

    public TaskDetailsViewModelModule(@Nullable Bundle savedState, @NonNull String taskId) {
        super(savedState);

        mTaskId = taskId;
    }

    @PerFragment
    @Provides
    TaskDetailsViewModel providesTaskDetailsViewModel(@NonNull GroupRepository groupRepository,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull TaskRepository taskRepository) {
        return new TaskDetailsViewModelImpl(mSavedState, userRepository, groupRepository,
                taskRepository, mTaskId);
    }

}
