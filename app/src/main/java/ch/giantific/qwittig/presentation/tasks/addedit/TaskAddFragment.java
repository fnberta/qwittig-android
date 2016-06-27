/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskAddComponent;

/**
 * Provides an interface for the user to add a new {@link Task}. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskAddFragment extends BaseTaskAddEditFragment<TaskAddComponent> {

    public TaskAddFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull TaskAddComponent component) {
        component.inject(this);
    }
}
