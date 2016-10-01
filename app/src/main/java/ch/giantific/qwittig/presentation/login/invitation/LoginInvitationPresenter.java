package ch.giantific.qwittig.presentation.login.invitation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginInvitationPresenter extends BasePresenterImpl<LoginInvitationContract.ViewListener>
        implements LoginInvitationContract.Presenter {

    private static final String STATE_VIEW_MODEL = LoginInvitationViewModel.class.getCanonicalName();
    private final LoginInvitationViewModel viewModel;

    public LoginInvitationPresenter(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull UserRepository userRepo) {
        super(savedState, navigator, userRepo);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new LoginInvitationViewModel();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public LoginInvitationViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void onAcceptClick(View view) {
        this.view.showAccountsScreen(true);
        this.view.showMessage(R.string.toast_invitation_accept_login);
    }

    @Override
    public void onDeclineClick(View view) {
        this.view.showAccountsScreen(false);
        this.view.showMessage(R.string.toast_invitation_ignore);
    }
}
