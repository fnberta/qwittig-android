/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.push.di;

import ch.giantific.qwittig.data.push.FcmInstanceIdService;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerService;
import dagger.Component;

/**
 * Provides the dependencies for the push broadcast receiver.
 *
 * @see {@link PushBroadcastReceiver}
 */
@PerService
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface FcmServiceComponent {

    void inject(FcmInstanceIdService fcmInstanceIdService);
}
