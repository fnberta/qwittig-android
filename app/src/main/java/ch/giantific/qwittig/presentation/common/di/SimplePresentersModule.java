package ch.giantific.qwittig.presentation.common.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.about.AboutContract;
import ch.giantific.qwittig.presentation.about.AboutPresenter;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditContract;
import ch.giantific.qwittig.presentation.assignments.addedit.add.AssignmentAddPresenter;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsContract;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsPresenter;
import ch.giantific.qwittig.presentation.camera.CameraContract;
import ch.giantific.qwittig.presentation.camera.CameraPresenter;
import ch.giantific.qwittig.presentation.finance.FinanceHeaderContract;
import ch.giantific.qwittig.presentation.finance.FinanceHeaderPresenter;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidContract;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidPresenter;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidContract;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidPresenter;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackContract;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackPresenter;
import ch.giantific.qwittig.presentation.login.accounts.LoginAccountsContract;
import ch.giantific.qwittig.presentation.login.accounts.LoginAccountsPresenter;
import ch.giantific.qwittig.presentation.login.accounts.LoginAccountsViewModel;
import ch.giantific.qwittig.presentation.login.email.LoginEmailContract;
import ch.giantific.qwittig.presentation.login.email.LoginEmailPresenter;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupContract;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupPresenter;
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationContract;
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationPresenter;
import ch.giantific.qwittig.presentation.login.profile.LoginProfileContract;
import ch.giantific.qwittig.presentation.login.profile.LoginProfilePresenter;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerContract;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddPresenter;
import ch.giantific.qwittig.presentation.purchases.list.HomeContract;
import ch.giantific.qwittig.presentation.purchases.list.HomePresenter;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsContract;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsPresenter;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesContract;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesPresenter;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingContract;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingPresenter;
import ch.giantific.qwittig.presentation.settings.general.SettingsContract;
import ch.giantific.qwittig.presentation.settings.general.SettingsPresenter;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupContract;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupPresenter;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersContract;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersPresenter;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileContract;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfilePresenter;
import ch.giantific.qwittig.presentation.stats.StatsContract;
import ch.giantific.qwittig.presentation.stats.StatsPresenter;
import dagger.Binds;
import dagger.Module;

/**
 * Created by fabio on 08.10.16.
 */

@Module
public abstract class SimplePresentersModule {

    @PerActivity
    @Binds
    abstract NavDrawerContract.Presenter bindsNavDrawerPresenter(NavDrawerPresenter navDrawerPresenter);

    @PerActivity
    @Binds
    abstract HomeContract.Presenter bindsHomePresenter(HomePresenter homePresenter);

    @PerActivity
    @Binds
    abstract PurchasesContract.Presenter bindsHomePurchasesPresenter(PurchasesPresenter purchasesPresenter);

    @PerActivity
    @Binds
    abstract DraftsContract.Presenter bindsHomeDraftsPresenter(DraftsPresenter draftsPresenter);

    @PerActivity
    @Binds
    abstract PurchaseAddEditContract.Presenter bindsPurchaseAddPresenter(PurchaseAddPresenter purchaseAddPresenter);

    @PerActivity
    @Binds
    abstract PurchaseAddEditContract.AddOcrPresenter bindsPurchaseAddOcrPresenter(PurchaseAddOcrPresenter purchaseAddOcrPresenter);

    @PerActivity
    @Binds
    abstract FinanceHeaderContract.Presenter bindsFinanceHeaderPresenter(FinanceHeaderPresenter financeHeaderPresenter);

    @PerActivity
    @Binds
    abstract CompsUnpaidContract.Presenter bindsFinanceCompsUnpaidPresenter(CompsUnpaidPresenter compsUnpaidPresenter);

    @PerActivity
    @Binds
    abstract OcrRatingContract.Presenter bindsOcrRatingPresenter(OcrRatingPresenter ocrRatingPresenter);

    @PerActivity
    @Binds
    abstract AboutContract.Presenter bindsAboutPresenter(AboutPresenter aboutPresenter);

    @PerActivity
    @Binds
    abstract HelpFeedbackContract.Presenter bindsHelpFeedbackPresenter(HelpFeedbackPresenter helpFeedbackPresenter);

    @PerActivity
    @Binds
    abstract LoginAccountsContract.Presenter bindsLoginAccountsPresenter(LoginAccountsPresenter loginAccountsPresenter);

    @PerActivity
    @Binds
    abstract LoginEmailContract.Presenter bindsLoginEmailPresenter(LoginEmailPresenter loginEmailPresenter);

    @PerActivity
    @Binds
    abstract LoginFirstGroupContract.Presenter bindsLoginFirstGroupPresenter(LoginFirstGroupPresenter loginFirstGroupPresenter);

    @PerActivity
    @Binds
    abstract LoginInvitationContract.Presenter bindsLoginInvitationPresenter(LoginInvitationPresenter loginInvitationPresenter);

    @PerActivity
    @Binds
    abstract LoginProfileContract.Presenter bindsLoginProfilePresenter(LoginProfilePresenter loginProfilePresenter);

    @PerActivity
    @Binds
    abstract StatsContract.Presenter bindsStatsPresenter(StatsPresenter statsPresenter);

    @PerActivity
    @Binds
    abstract SettingsContract.Presenter bindsSettingsPresenter(SettingsPresenter settingsPresenter);

    @PerActivity
    @Binds
    abstract SettingsAddGroupContract.Presenter bindsSettingsAddGroupPresenter(SettingsAddGroupPresenter settingsAddGroupPresenter);

    @PerActivity
    @Binds
    abstract SettingsUsersContract.Presenter bindsSettingsUsersPresenter(SettingsUsersPresenter settingsUsersPresenter);

    @PerActivity
    @Binds
    abstract SettingsProfileContract.Presenter bindsSettingsProfilePresenter(SettingsProfilePresenter settingsProfilePresenter);

    @PerActivity
    @Binds
    abstract CameraContract.Presenter bindsCameraPresenter(CameraPresenter cameraPresenter);

    @PerActivity
    @Binds
    abstract AssignmentsContract.Presenter bindsAssignmentsPresenter(AssignmentsPresenter assignmentsPresenter);

    @PerActivity
    @Binds
    abstract AssignmentAddEditContract.Presenter bindsAssignmentAddPresenter(AssignmentAddPresenter assignmentAddPresenter);
}
