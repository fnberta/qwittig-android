/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingActivity;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingDetailsFragment;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase add screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class, NavigatorModule.class, OcrRatingViewModelModule.class})
public interface OcrRatingComponent {

    void inject(OcrRatingActivity ocrRatingActivity);

    void inject(OcrRatingFragment ocrRatingFragment);

    void inject(OcrRatingDetailsFragment ocrRatingDetailsFragment);
}
