/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.IdentitiesUpdateWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.OcrWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.RatesWorker;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import ch.giantific.qwittig.presentation.settings.LogoutWorker;
import ch.giantific.qwittig.presentation.settings.addgroup.AddGroupWorker;
import ch.giantific.qwittig.presentation.home.InvitedGroupWorker;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker;
import ch.giantific.qwittig.presentation.stats.StatsCalcWorker;
import ch.giantific.qwittig.presentation.settings.addusers.AddUserWorker;
import ch.giantific.qwittig.presentation.finance.CompensationsQueryMoreWorker;
import ch.giantific.qwittig.presentation.finance.CompensationsUpdateWorker;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesQueryMoreWorker;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesUpdateWorker;
import ch.giantific.qwittig.presentation.tasks.list.TasksUpdateWorker;
import ch.giantific.qwittig.presentation.finance.CompensationRemindWorker;
import ch.giantific.qwittig.presentation.tasks.list.TaskRemindWorker;
import ch.giantific.qwittig.presentation.finance.CompensationSaveWorker;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseSaveWorker;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {RepositoriesModule.class})
public interface WorkerComponent {

    void inject(CompensationsUpdateWorker compensationsUpdateWorker);

    void inject(PurchaseSaveWorker purchaseSaveWorker);

    void inject(CompensationSaveWorker compensationSaveWorker);

    void inject(PurchasesUpdateWorker purchasesUpdateWorker);

    void inject(RatesWorker ratesWorker);

    void inject(OcrWorker ocrWorker);

    void inject(AddUserWorker addUserWorker);

    void inject(StatsCalcWorker statsCalcWorker);

    void inject(InvitedGroupWorker invitedGroupWorker);

    void inject(AddGroupWorker addGroupWorker);

    void inject(TaskRemindWorker taskRemindWorker);

    void inject(CompensationRemindWorker compensationRemindWorker);

    void inject(CompensationsQueryMoreWorker compensationsQueryMoreWorker);

    void inject(PurchasesQueryMoreWorker purchasesQueryMoreWorker);

    void inject(TasksUpdateWorker tasksUpdateWorker);

    void inject(IdentitiesUpdateWorker identitiesUpdateWorker);

    void inject(LoginWorker loginWorker);

    void inject(LogoutWorker logoutWorker);

    void inject(UnlinkThirdPartyWorker unlinkThirdPartyWorker);
}
