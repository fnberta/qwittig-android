/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackFragment;
import dagger.Component;

/**
 * Provides the dependencies for the help and feedback screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {HelpFeedbackViewModelModule.class, RepositoriesModule.class})
public interface HelpFeedbackComponent {

    void inject(HelpFeedbackFragment helpFeedbackFragment);
}
