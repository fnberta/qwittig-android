/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidFragment;
import dagger.Component;

/**
 * Provides the dependencies for the paid compensations screen.
 */
@PerFragment
@Component(modules = {FinanceCompsPaidViewModelModule.class, RepositoriesModule.class})
public interface FinanceCompsPaidComponent {

    void inject(CompsPaidFragment compsPaidFragment);
}
