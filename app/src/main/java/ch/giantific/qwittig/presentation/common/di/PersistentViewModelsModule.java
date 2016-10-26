package ch.giantific.qwittig.presentation.common.di;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.AssignmentAddEditViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.AssignmentDetailsViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.AssignmentsViewModel;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.login.accounts.LoginAccountsViewModel;
import ch.giantific.qwittig.presentation.login.email.LoginEmailViewModel;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupViewModel;
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationViewModel;
import ch.giantific.qwittig.presentation.login.profile.LoginProfileViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.HomeViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingViewModel;
import ch.giantific.qwittig.presentation.settings.general.SettingsViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 08.10.16.
 */

@Module
public class PersistentViewModelsModule {

    @Nullable
    private final Bundle savedState;

    public PersistentViewModelsModule(@Nullable Bundle savedState) {
        this.savedState = savedState;
    }

    @PerActivity
    @Provides
    HomeViewModel providesHomeViewModel() {
        return savedState != null
               ? savedState.getParcelable(HomeViewModel.TAG)
               : new HomeViewModel();
    }

    @PerActivity
    @Provides
    PurchasesViewModel providesPurchasesViewModel() {
        return savedState != null
               ? savedState.getParcelable(PurchasesViewModel.TAG)
               : new PurchasesViewModel();
    }

    @PerActivity
    @Provides
    PurchaseDetailsViewModel providesPurchaseDetailsViewModel() {
        return savedState != null
               ? savedState.getParcelable(PurchaseDetailsViewModel.TAG)
               : new PurchaseDetailsViewModel();
    }

    @PerActivity
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddEditViewModel() {
        return savedState != null
               ? savedState.getParcelable(PurchaseAddEditViewModel.TAG)
               : new PurchaseAddEditViewModel();
    }

    @PerActivity
    @Provides
    DraftsViewModel providesDraftsViewModel() {
        return savedState != null
               ? savedState.getParcelable(DraftsViewModel.TAG)
               : new DraftsViewModel();
    }

    @PerActivity
    @Provides
    OcrRatingViewModel providesOcrRatingViewModel() {
        return savedState != null
               ? savedState.getParcelable(OcrRatingViewModel.TAG)
               : new OcrRatingViewModel();
    }

    @PerActivity
    @Provides
    CompsPaidViewModel providesCompsPaidViewModel() {
        return savedState != null
               ? savedState.getParcelable(CompsPaidViewModel.TAG)
               : new CompsPaidViewModel();
    }

    @PerActivity
    @Provides
    CompsUnpaidViewModel providesCompsUnpaidViewModel() {
        return savedState != null
               ? savedState.getParcelable(CompsUnpaidViewModel.TAG)
               : new CompsUnpaidViewModel();
    }

    @PerActivity
    @Provides
    AssignmentsViewModel providesAssignmentsViewModel() {
        return savedState != null
               ? savedState.getParcelable(AssignmentsViewModel.TAG)
               : new AssignmentsViewModel();
    }

    @PerActivity
    @Provides
    AssignmentDetailsViewModel providesAssignmentDetailsViewModel() {
        return savedState != null
               ? savedState.getParcelable(AssignmentDetailsViewModel.TAG)
               : new AssignmentDetailsViewModel();
    }

    @PerActivity
    @Provides
    AssignmentAddEditViewModel providesAssignmentAddEditViewModel() {
        return savedState != null
               ? savedState.getParcelable(AssignmentAddEditViewModel.TAG)
               : new AssignmentAddEditViewModel();
    }

    @PerActivity
    @Provides
    StatsViewModel providesStatsViewModel() {
        return savedState != null
               ? savedState.getParcelable(StatsViewModel.TAG)
               : new StatsViewModel();
    }

    @PerActivity
    @Provides
    SettingsViewModel providesSettingsViewModel() {
        return savedState != null
               ? savedState.getParcelable(SettingsViewModel.TAG)
               : new SettingsViewModel();
    }

    @PerActivity
    @Provides
    SettingsAddGroupViewModel providesSettingsAddGroupViewModel() {
        return savedState != null
               ? savedState.getParcelable(SettingsAddGroupViewModel.TAG)
               : new SettingsAddGroupViewModel();
    }

    @PerActivity
    @Provides
    SettingsUsersViewModel providesSettingsUsersViewModel() {
        return savedState != null
               ? savedState.getParcelable(SettingsUsersViewModel.TAG)
               : new SettingsUsersViewModel();
    }

    @PerActivity
    @Provides
    SettingsProfileViewModel providesSettingsProfileViewModel() {
        return savedState != null
               ? savedState.getParcelable(SettingsProfileViewModel.TAG)
               : new SettingsProfileViewModel();
    }

    @PerActivity
    @Provides
    LoginAccountsViewModel providesLoginAccountsViewModel() {
        return savedState != null
               ? savedState.getParcelable(LoginAccountsViewModel.TAG)
               : new LoginAccountsViewModel();
    }

    @PerActivity
    @Provides
    LoginEmailViewModel providesLoginEmailViewModel() {
        return savedState != null
               ? savedState.getParcelable(LoginEmailViewModel.TAG)
               : new LoginEmailViewModel();
    }

    @PerActivity
    @Provides
    LoginFirstGroupViewModel providesLoginFirstGroupViewModel() {
        return savedState != null
               ? savedState.getParcelable(LoginFirstGroupViewModel.TAG)
               : new LoginFirstGroupViewModel();
    }

    @PerActivity
    @Provides
    LoginInvitationViewModel providesLoginInvitationViewModel() {
        return savedState != null
               ? savedState.getParcelable(LoginInvitationViewModel.TAG)
               : new LoginInvitationViewModel();
    }

    @PerActivity
    @Provides
    LoginProfileViewModel providesLoginProfileViewModel() {
        return savedState != null
               ? savedState.getParcelable(LoginProfileViewModel.TAG)
               : new LoginProfileViewModel();
    }
}
