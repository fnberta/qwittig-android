/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.di;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.home.HomeViewModel;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the home screen.
 */
@PerScreen
@Subcomponent(modules = {HomeViewModelModule.class})
public interface HomeSubcomponent {

    void inject(HomeActivity homeActivity);

    HomeViewModel getHomeViewModel();
}
