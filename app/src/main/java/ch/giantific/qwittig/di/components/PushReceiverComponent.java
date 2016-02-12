/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.modules.SystemServiceModule;
import ch.giantific.qwittig.di.scopes.PerBroadcastReceive;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerBroadcastReceive
@Component(dependencies = {ApplicationComponent.class},
        modules = {SystemServiceModule.class, RepositoriesModule.class})
public interface PushReceiverComponent {

    void inject(PushBroadcastReceiver pushBroadcastReceiver);
}
