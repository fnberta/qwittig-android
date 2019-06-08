/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.jobs.di;

import ch.giantific.qwittig.data.jobs.UploadAvatarJob;
import ch.giantific.qwittig.data.jobs.UploadReceiptJob;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerService;
import dagger.Component;

/**
 * Provides the dependencies for the parse query and the save purchase services.
 */
@PerService
@Component(dependencies = {ApplicationComponent.class})
public interface JobsComponent {
    void inject(UploadReceiptJob uploadReceiptJob);

    void inject(UploadAvatarJob uploadAvatarJob);
}
