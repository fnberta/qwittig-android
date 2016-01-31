/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.workerfragments.OcrWorker;
import ch.giantific.qwittig.presentation.workerfragments.RatesWorker;
import ch.giantific.qwittig.presentation.workerfragments.account.LoginWorker;
import ch.giantific.qwittig.presentation.workerfragments.account.LogoutWorker;
import ch.giantific.qwittig.presentation.workerfragments.account.UnlinkThirdPartyWorker;
import ch.giantific.qwittig.presentation.workerfragments.group.CreateGroupWorker;
import ch.giantific.qwittig.presentation.workerfragments.group.InvitedGroupWorker;
import ch.giantific.qwittig.presentation.workerfragments.group.StatsCalcWorker;
import ch.giantific.qwittig.presentation.workerfragments.group.UsersInviteWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsQueryMoreWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesQueryMoreWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.TasksUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.UsersUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.reminder.CompensationRemindWorker;
import ch.giantific.qwittig.presentation.workerfragments.reminder.TaskRemindWorker;
import ch.giantific.qwittig.presentation.workerfragments.save.CompensationSaveWorker;
import ch.giantific.qwittig.presentation.workerfragments.save.PurchaseSaveWorker;
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

    void inject(UsersInviteWorker usersInviteWorker);

    void inject(StatsCalcWorker statsCalcWorker);

    void inject(InvitedGroupWorker invitedGroupWorker);

    void inject(CreateGroupWorker createGroupWorker);

    void inject(TaskRemindWorker taskRemindWorker);

    void inject(CompensationRemindWorker compensationRemindWorker);

    void inject(CompensationsQueryMoreWorker compensationsQueryMoreWorker);

    void inject(PurchasesQueryMoreWorker purchasesQueryMoreWorker);

    void inject(TasksUpdateWorker tasksUpdateWorker);

    void inject(UsersUpdateWorker usersUpdateWorker);
}
