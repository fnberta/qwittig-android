/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorker;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorker;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorker;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import ch.giantific.qwittig.presentation.purchases.addedit.RatesWorker;
import ch.giantific.qwittig.presentation.tasks.list.TaskRemindWorker;
import dagger.Component;

/**
 * Defines the dependencies for headless worker fragments.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface WorkerComponent {

    void inject(RatesWorker ratesWorker);

    void inject(TaskRemindWorker taskRemindWorker);

    void inject(LoginWorker loginWorker);

    void inject(EmailUserWorker emailUserWorker);

    void inject(GoogleUserWorker googleUserWorker);

    void inject(FacebookUserWorker facebookUserWorker);
}
