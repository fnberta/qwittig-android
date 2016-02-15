/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.di.modules.LocalBroadcastModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerBroadcastReceive;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerBroadcastReceive
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class, LocalBroadcastModule.class})
public interface QueryServiceComponent {
    void inject(ParseQueryService parseQueryService);
}
