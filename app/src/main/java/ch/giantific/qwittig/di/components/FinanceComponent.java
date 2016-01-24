/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.FinanceViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.FinanceCompensationsPaidFragment;
import ch.giantific.qwittig.presentation.ui.fragments.FinanceCompensationsUnpaidFragment;
import ch.giantific.qwittig.presentation.ui.fragments.FinanceUserBalancesFragment;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsPaidViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModel;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {FinanceViewModelModule.class, RepositoriesModule.class})
public interface FinanceComponent {

    void inject(FinanceUserBalancesFragment financeUserBalancesFragment);

    void inject(FinanceCompensationsUnpaidFragment financeCompensationsUnpaidFragment);

    void inject(FinanceCompensationsPaidFragment financeCompensationsPaidFragment);

    FinanceUsersViewModel getFinanceUsersViewModel();

    FinanceCompsUnpaidViewModel getFinanceCompsUnpaidViewModel();

    FinanceCompsPaidViewModel getFinanceCompsPaidViewModel();
}
