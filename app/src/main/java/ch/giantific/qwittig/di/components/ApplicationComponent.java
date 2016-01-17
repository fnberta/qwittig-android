/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import android.app.Application;

import javax.inject.Singleton;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.di.modules.ApplicationModule;
import ch.giantific.qwittig.di.modules.RestServiceModule;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@Singleton
@Component(modules = {ApplicationModule.class, RestServiceModule.class})
public interface ApplicationComponent {

    Application getApplication();
}
