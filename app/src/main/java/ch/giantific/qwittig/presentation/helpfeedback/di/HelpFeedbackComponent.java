/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackFragment;
import dagger.Component;

/**
 * Created by fabio on 05.02.16.
 */
@PerFragment
@Component(modules = {HelpFeedbackViewModelModule.class, RepositoriesModule.class})
public interface HelpFeedbackComponent {

    void inject(HelpFeedbackFragment helpFeedbackFragment);
}
