/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.CompRemindWorker;
import ch.giantific.qwittig.presentation.finance.CompSaveWorker;
import ch.giantific.qwittig.presentation.finance.CompsQueryMoreWorker;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesUpdateWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.OcrWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.RatesWorker;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import ch.giantific.qwittig.presentation.settings.general.LogoutWorker;
import ch.giantific.qwittig.presentation.settings.addgroup.AddGroupWorker;
import ch.giantific.qwittig.presentation.home.JoinGroupWorker;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileWorker;
import ch.giantific.qwittig.presentation.stats.StatsCalcWorker;
import ch.giantific.qwittig.presentation.settings.users.AddUserWorker;
import ch.giantific.qwittig.presentation.finance.CompsUpdateWorker;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesQueryMoreWorker;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesUpdateWorker;
import ch.giantific.qwittig.presentation.tasks.list.TasksUpdateWorker;
import ch.giantific.qwittig.presentation.tasks.list.TaskRemindWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseSaveWorker;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface WorkerComponent {

    void inject(CompsUpdateWorker compsUpdateWorker);

    void inject(PurchaseSaveWorker purchaseSaveWorker);

    void inject(CompSaveWorker compSaveWorker);

    void inject(PurchasesUpdateWorker purchasesUpdateWorker);

    void inject(RatesWorker ratesWorker);

    void inject(OcrWorker ocrWorker);

    void inject(AddUserWorker addUserWorker);

    void inject(StatsCalcWorker statsCalcWorker);

    void inject(JoinGroupWorker joinGroupWorker);

    void inject(AddGroupWorker addGroupWorker);

    void inject(TaskRemindWorker taskRemindWorker);

    void inject(CompRemindWorker compRemindWorker);

    void inject(CompsQueryMoreWorker compsQueryMoreWorker);

    void inject(PurchasesQueryMoreWorker purchasesQueryMoreWorker);

    void inject(TasksUpdateWorker tasksUpdateWorker);

    void inject(IdentitiesUpdateWorker identitiesUpdateWorker);

    void inject(LoginWorker loginWorker);

    void inject(LogoutWorker logoutWorker);

    void inject(SettingsProfileWorker settingsProfileWorker);
}
