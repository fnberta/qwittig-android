/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModel;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidFragment;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidViewModel;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the finance screen.
 */
@PerActivity
@Subcomponent(modules = {FinanceHeaderViewModelModule.class,
        FinanceCompsUnpaidViewModelModule.class, FinanceCompsPaidViewModelModule.class})
public interface FinanceSubcomponent {

    void inject(FinanceActivity financeActivity);

    void inject(CompsUnpaidFragment compsUnpaidFragment);

    void inject(CompsPaidFragment compsPaidFragment);
}
