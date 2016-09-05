/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsSubcomponent;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsPaidViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsUnpaidViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceHeaderViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.di.DraftsListViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.di.PurchasesListViewModelModule;
import ch.giantific.qwittig.presentation.stats.di.StatsLoaderModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.di.StatsViewModelModule;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsViewModelModule;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsViewModelModule;
import dagger.Component;

/**
 * Provides the dependencies for the navigation drawer.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {NavDrawerViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface NavDrawerComponent {

    HomeSubcomponent plus(HomeViewModelModule homeViewModelModule,
                          GoogleApiClientDelegateModule googleApiClientDelegateModule,
                          PurchasesListViewModelModule purchasesListViewModelModule,
                          DraftsListViewModelModule draftsListViewModelModule);

    FinanceSubcomponent plus(FinanceHeaderViewModelModule financeHeaderViewModelModule,
                             FinanceCompsUnpaidViewModelModule compsUnpaidViewModelModule,
                             FinanceCompsPaidViewModelModule compsPaidViewModelModule);

    PurchaseDetailsSubcomponent plus(PurchaseDetailsViewModelModule purchaseDetailsViewModelModule);

    AssignmentsSubcomponent plus(AssignmentsViewModelModule assignmentsViewModelModule);

    AssignmentDetailsSubcomponent plus(AssignmentDetailsViewModelModule assignmentDetailsViewModelModule);

    StatsSubcomponent plus(StatsViewModelModule statsViewModelModule,
                           StatsLoaderModule statsLoaderModule);
}
