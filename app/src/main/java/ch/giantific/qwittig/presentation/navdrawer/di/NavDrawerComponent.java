/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;
import dagger.Component;

/**
 * Provides the dependencies for the navigation drawer.
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
