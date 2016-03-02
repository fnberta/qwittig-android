/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.HomeViewModel;
import dagger.Component;

/**
 * Provides the dependencies for the home screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {HomeViewModelModule.class, RepositoriesModule.class})
public interface HomeComponent {

    HomeViewModel getHomeViewModel();
}
