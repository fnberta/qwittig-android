/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.about.AboutActivity;
import ch.giantific.qwittig.presentation.about.AboutFragment;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import dagger.Component;

/**
 * Provides the dependencies for the help and feedback screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, NavigatorModule.class})
public interface AboutComponent {

    void inject(AboutActivity aboutActivity);

    void inject(AboutFragment aboutFragment);
}
