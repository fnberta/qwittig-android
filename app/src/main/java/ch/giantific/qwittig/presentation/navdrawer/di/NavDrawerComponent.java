/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsPresenterModule;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsSubcomponent;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsPaidPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsPresenterModule;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.stats.di.StatsLoaderModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import dagger.Component;

/**
 * Provides the dependencies for the navigation drawer.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, NavigatorModule.class})
public interface NavDrawerComponent {

    HomeSubcomponent plus(PersistentViewModelsModule persistentViewModelsModule,
                          GoogleApiClientDelegateModule googleApiClientDelegateModule);

    FinanceSubcomponent plus(FinanceCompsPaidPresenterModule compsPaidViewModelModule,
                             PersistentViewModelsModule persistentViewModelsModule);

    PurchaseDetailsSubcomponent plus(PurchaseDetailsPresenterModule purchaseDetailsViewModelModule,
                                     PersistentViewModelsModule persistentViewModelsModule);

    AssignmentsSubcomponent plus(PersistentViewModelsModule persistentViewModelsModule);

    AssignmentDetailsSubcomponent plus(AssignmentDetailsPresenterModule assignmentDetailsViewModelModule,
                                       PersistentViewModelsModule persistentViewModelsModule);

    StatsSubcomponent plus(PersistentViewModelsModule persistentViewModelsModule,
                           StatsLoaderModule statsLoaderModule);
}
