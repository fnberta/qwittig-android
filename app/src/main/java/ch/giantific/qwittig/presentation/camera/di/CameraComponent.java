/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.camera.CameraActivity;
import ch.giantific.qwittig.presentation.camera.CameraActivity2;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import dagger.Component;

/**
 * Provides the dependencies for the help and feedback screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, NavigatorModule.class})
public interface CameraComponent {

    void inject(CameraActivity cameraActivity);

    void inject(CameraActivity2 cameraActivity2);
}
