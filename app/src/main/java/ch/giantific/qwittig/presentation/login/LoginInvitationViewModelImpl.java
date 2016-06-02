package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginInvitationViewModelImpl extends ViewModelBaseImpl<LoginInvitationViewModel.ViewListener>
        implements LoginInvitationViewModel {

    private String mGroupName;
    private String mInviterNickname;

    public LoginInvitationViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull LoginInvitationViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository,
                                        @NonNull String groupName,
                                        @NonNull String inviterNickname) {
        super(savedState, view, userRepository);

        mGroupName = groupName;
        mInviterNickname = inviterNickname;
    }

    @Override
    @Bindable
    public String getGroupName() {
        return mGroupName;
    }

    @Override
    @Bindable
    public String getInviterNickname() {
        return mInviterNickname;
    }

    @Override
    public void onAcceptClick(View view) {
        mView.showAccountsFragment(true);
        mView.showMessage(R.string.toast_invitation_accept_login);
    }

    @Override
    public void onDeclineClick(View view) {
        mView.showAccountsFragment(false);
        mView.showMessage(R.string.toast_invitation_ignore);
    }
}
