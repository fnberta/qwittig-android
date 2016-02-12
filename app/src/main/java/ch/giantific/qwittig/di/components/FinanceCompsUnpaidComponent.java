/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.FinanceCompsUnpaidViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.CompsUnpaidFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {FinanceCompsUnpaidViewModelModule.class, RepositoriesModule.class})
public interface FinanceCompsUnpaidComponent {

    void inject(CompsUnpaidFragment compsUnpaidFragment);
}
