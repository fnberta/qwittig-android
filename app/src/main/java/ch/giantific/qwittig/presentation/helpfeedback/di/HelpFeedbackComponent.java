/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackActivity;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackFragment;
import dagger.Component;

/**
 * Provides the dependencies for the help and feedback screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, NavigatorModule.class})
public interface HelpFeedbackComponent {

    void inject(HelpFeedbackActivity helpFeedbackActivity);

    void inject(HelpFeedbackFragment helpFeedbackFragment);
}
