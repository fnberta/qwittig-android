/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.push.di;

import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.SystemServiceModule;
import ch.giantific.qwittig.di.scopes.PerBroadcastReceive;
import dagger.Component;

/**
 * Provides the dependencies for the push broadcast receiver.
 *
 * @see {@link PushBroadcastReceiver}
 */
@PerBroadcastReceive
@Component(dependencies = {ApplicationComponent.class},
        modules = {SystemServiceModule.class, RepositoriesModule.class})
public interface PushReceiverComponent {

    void inject(PushBroadcastReceiver pushBroadcastReceiver);
}
