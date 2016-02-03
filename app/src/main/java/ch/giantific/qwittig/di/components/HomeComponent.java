/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.viewmodels.HomeDraftsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomeViewModel;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {HomeViewModelModule.class, RepositoriesModule.class})
public interface HomeComponent {

    void inject(HomePurchasesFragment homePurchasesFragment);

    void inject(HomeDraftsFragment homeDraftsFragment);

    HomePurchasesViewModel getHomePurchasesViewModel();

    HomeDraftsViewModel getHomeDraftsViewModel();

    HomeViewModel getHomeViewModel();
}
