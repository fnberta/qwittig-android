/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.services.di;

import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerBroadcastReceive;
import dagger.Component;

/**
 * Provides the dependencies for the parse query service.
 *
 * @see {@link ParseQueryService}
 */
@PerBroadcastReceive
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface QueryServiceComponent {
    void inject(ParseQueryService parseQueryService);
}
