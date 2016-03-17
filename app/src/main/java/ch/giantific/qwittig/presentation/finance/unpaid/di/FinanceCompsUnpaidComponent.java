/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidFragment;
import dagger.Component;

/**
 * Provides the dependencies for the unpaid compensations screen.
 */
@PerScreen
@Component(modules = {FinanceCompsUnpaidViewModelModule.class, RepositoriesModule.class})
public interface FinanceCompsUnpaidComponent {

    void inject(CompsUnpaidFragment compsUnpaidFragment);
}
