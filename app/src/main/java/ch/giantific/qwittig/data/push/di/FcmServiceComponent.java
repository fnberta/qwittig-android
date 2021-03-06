/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.push.di;

import ch.giantific.qwittig.data.push.FcmInstanceIdService;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerService;
import dagger.Component;

/**
 * Provides the dependencies for the fcm instance id service.
 *
 * @see {@link FcmInstanceIdService}
 */
@PerService
@Component(dependencies = {ApplicationComponent.class})
public interface FcmServiceComponent {

    void inject(FcmInstanceIdService fcmInstanceIdService);

    void inject(FcmMessagingService fcmMessagingService);
}
