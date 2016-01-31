/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.modules.TaskAddViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.TaskAddEditFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {TaskAddViewModelModule.class, RepositoriesModule.class})
public interface TaskAddComponent {

    void inject(TaskAddEditFragment taskAddEditFragment);
}
