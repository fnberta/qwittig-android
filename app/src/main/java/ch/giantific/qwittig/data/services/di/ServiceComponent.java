/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.services.di;

import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.data.services.SavePurchaseTaskService;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerService;
import dagger.Component;

/**
 * Provides the dependencies for the parse query and the save purchase services.
 */
@PerService
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface ServiceComponent {
    void inject(ParseQueryService parseQueryService);

    void inject(SavePurchaseTaskService savePurchaseTaskService);
}
