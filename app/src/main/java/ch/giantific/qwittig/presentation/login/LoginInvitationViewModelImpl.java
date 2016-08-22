package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginInvitationViewModelImpl extends ViewModelBaseImpl<LoginInvitationViewModel.ViewListener>
        implements LoginInvitationViewModel {

    private String groupName;
    private String inviterNickname;

    public LoginInvitationViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);
    }

    @Override
    @Bindable
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
    }

    @Override
    @Bindable
    public String getInviterNickname() {
        return inviterNickname;
    }

    @Override
    public void setInviterNickname(@NonNull String inviterNickname) {
        this.inviterNickname = inviterNickname;
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
