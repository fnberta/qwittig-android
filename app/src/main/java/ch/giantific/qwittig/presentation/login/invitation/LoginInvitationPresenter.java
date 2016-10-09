package ch.giantific.qwittig.presentation.login.invitation;

import android.support.annotation.NonNull;
import android.view.View;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginInvitationPresenter extends BasePresenterImpl<LoginInvitationContract.ViewListener>
        implements LoginInvitationContract.Presenter {

    private final LoginInvitationViewModel viewModel;

    @Inject
    public LoginInvitationPresenter(@NonNull Navigator navigator,
                                    @NonNull LoginInvitationViewModel viewModel,
                                    @NonNull UserRepository userRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
    }

    @Override
    public void onAcceptClick(View view) {
        this.view.showAccountsLogin(true);
        this.view.showMessage(R.string.toast_invitation_accept_login);
    }

    @Override
    public void onDeclineClick(View view) {
        this.view.showAccountsLogin(false);
        this.view.showMessage(R.string.toast_invitation_ignore);
    }
}
