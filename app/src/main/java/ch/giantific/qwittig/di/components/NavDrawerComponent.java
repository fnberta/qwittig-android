/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.NavDrawerViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.ui.activities.FinanceActivity;
import ch.giantific.qwittig.presentation.ui.activities.HomeActivity;
import ch.giantific.qwittig.presentation.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.ui.activities.TasksActivity;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerActivity
@Component(modules = {NavDrawerViewModelModule.class, RepositoriesModule.class})
public interface NavDrawerComponent {

    void inject(HomeActivity homeActivity);

    void inject(FinanceActivity financeActivity);

    void inject(TasksActivity tasksActivity);

    void inject(TaskDetailsActivity taskDetailsActivity);

    void inject(PurchaseDetailsActivity purchaseDetailsActivity);
}
