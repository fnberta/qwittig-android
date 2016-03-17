/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.finance.di.BalanceHeaderViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.home.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.home.di.HomeViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.stats.StatsActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;
import dagger.Component;

/**
 * Provides the dependencies for the navigation drawer.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {NavDrawerViewModelModule.class, RepositoriesModule.class})
public interface NavDrawerComponent {

    void inject(TasksActivity tasksActivity);

    void inject(TaskDetailsActivity taskDetailsActivity);

    void inject(PurchaseDetailsActivity purchaseDetailsActivity);

    void inject(StatsActivity statsActivity);

    HomeSubcomponent plus(HomeViewModelModule viewModelModule);

    FinanceSubcomponent plus(BalanceHeaderViewModelModule viewModelModule);
}
