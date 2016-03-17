/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModel;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the finance header screen.
 */
@PerScreen
@Subcomponent(modules = {BalanceHeaderViewModelModule.class})
public interface FinanceSubcomponent {

    void inject(FinanceActivity financeActivity);

    BalanceHeaderViewModel getBalanceHeaderViewModel();
}
