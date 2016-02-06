/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.FinanceCompsPaidViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.FinanceCompensationsPaidFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {FinanceCompsPaidViewModelModule.class, RepositoriesModule.class})
public interface FinanceCompsPaidComponent {

    void inject(FinanceCompensationsPaidFragment financeCompensationsPaidFragment);
}
