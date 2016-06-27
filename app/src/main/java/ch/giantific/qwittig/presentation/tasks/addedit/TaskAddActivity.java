/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.tasks.addedit.di.DaggerTaskAddComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskAddComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskAddViewModelModule;

/**
 * Hosts {@link TaskAddFragment} that allows the user to create a new task.
 * <p/>
 * Handles transition animations and displays the task's title in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class TaskAddActivity extends BaseTaskAddEditActivity<TaskAddComponent> {

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerTaskAddComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .taskAddViewModelModule(new TaskAddViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected BaseTaskAddEditFragment getTaskFragment() {
        return new TaskAddFragment();
    }
}
