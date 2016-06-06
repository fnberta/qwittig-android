/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.finance.paid.CompsQueryMoreWorker;
import ch.giantific.qwittig.presentation.finance.unpaid.CompRemindWorker;
import ch.giantific.qwittig.presentation.home.JoinGroupWorker;
import ch.giantific.qwittig.presentation.home.OcrWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.RatesWorker;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesQueryMoreWorker;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import ch.giantific.qwittig.presentation.settings.addgroup.AddGroupWorker;
import ch.giantific.qwittig.presentation.settings.general.LogoutWorker;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker;
import ch.giantific.qwittig.presentation.settings.users.AddUserWorker;
import ch.giantific.qwittig.presentation.tasks.list.TaskRemindWorker;
import dagger.Component;

/**
 * Defines the dependencies for headless worker fragments.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface WorkerComponent {

    void inject(RatesWorker ratesWorker);

    void inject(OcrWorker ocrWorker);

    void inject(AddUserWorker addUserWorker);

    void inject(AddGroupWorker addGroupWorker);

    void inject(JoinGroupWorker joinGroupWorker);

    void inject(TaskRemindWorker taskRemindWorker);

    void inject(CompRemindWorker compRemindWorker);

    void inject(CompsQueryMoreWorker compsQueryMoreWorker);

    void inject(PurchasesQueryMoreWorker purchasesQueryMoreWorker);

    void inject(LoginWorker loginWorker);

    void inject(LogoutWorker logoutWorker);

    void inject(UnlinkThirdPartyWorker settingsProfileWorker);
}
