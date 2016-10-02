/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsPresenterModule;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsPresenterModule;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsSubcomponent;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsPaidPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsUnpaidPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceHeaderPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsPresenterModule;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.di.DraftsPresenterModule;
import ch.giantific.qwittig.presentation.purchases.list.di.HomePresenterModule;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.di.PurchasesPresenterModule;
import ch.giantific.qwittig.presentation.stats.di.StatsLoaderModule;
import ch.giantific.qwittig.presentation.stats.di.StatsPresenterModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import dagger.Component;

/**
 * Provides the dependencies for the navigation drawer.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {NavDrawerPresenterModule.class, NavigatorModule.class})
public interface NavDrawerComponent {

    HomeSubcomponent plus(HomePresenterModule homeViewModelModule,
                          GoogleApiClientDelegateModule googleApiClientDelegateModule,
                          PurchasesPresenterModule purchasesListViewModelModule,
                          DraftsPresenterModule draftsListViewModelModule);

    FinanceSubcomponent plus(FinanceHeaderPresenterModule financeHeaderViewModelModule,
                             FinanceCompsUnpaidPresenterModule compsUnpaidViewModelModule,
                             FinanceCompsPaidPresenterModule compsPaidViewModelModule);

    PurchaseDetailsSubcomponent plus(PurchaseDetailsPresenterModule purchaseDetailsViewModelModule);

    AssignmentsSubcomponent plus(AssignmentsPresenterModule assignmentsViewModelModule);

    AssignmentDetailsSubcomponent plus(AssignmentDetailsPresenterModule assignmentDetailsViewModelModule);

    StatsSubcomponent plus(StatsPresenterModule statsViewModelModule,
                           StatsLoaderModule statsLoaderModule);
}
