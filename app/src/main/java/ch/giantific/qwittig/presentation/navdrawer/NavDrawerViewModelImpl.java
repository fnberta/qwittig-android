/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link NavDrawerViewModel}.
 */
public class NavDrawerViewModelImpl extends ViewModelBaseImpl<NavDrawerViewModel.ViewListener>
        implements NavDrawerViewModel {

    private final IdentityRepository mIdentityRepo;
    private final List<Identity> mIdentities = new ArrayList<>();

    public NavDrawerViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull NavDrawerViewModel.ViewListener view,
                                  @NonNull UserRepository userRepository,
                                  @NonNull IdentityRepository identityRepository) {
        super(savedState, view, userRepository);

        mIdentityRepo = identityRepository;
    }

    @Override
    public void onScreenVisible() {
        super.onScreenVisible();

        if (mCurrentUser != null) {
            loadIdentities();
        }
    }

    private void loadIdentities() {
        getSubscriptions().add(mIdentityRepo.fetchIdentitiesDataAsync(mCurrentUser.getIdentities())
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return identity.isActive();
                    }
                })
                .subscribe(new Subscriber<Identity>() {
                    @Override
                    public void onStart() {
                        super.onStart();

                        mIdentities.clear();
                    }

                    @Override
                    public void onCompleted() {
                        notifyPropertyChanged(BR.identityNickname);
                        mView.notifyHeaderIdentitiesChanged();
                        notifyPropertyChanged(BR.selectedIdentity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_drawer_load_groups);
                    }

                    @Override
                    public void onNext(Identity identity) {
                        mIdentities.add(identity);
                    }
                })
        );
    }

    @Override
    @Bindable
    public String getIdentityNickname() {
        return mCurrentIdentity.isDataAvailable() ? mCurrentIdentity.getNickname() : "";
    }

    @Override
    @Bindable
    public String getIdentityAvatar() {
        return mCurrentIdentity.isDataAvailable() ? mCurrentIdentity.getAvatarUrl() : "";
    }

    @Override
    @Bindable
    public int getSelectedIdentity() {
        return mIdentities.indexOf(mCurrentIdentity);
    }

    @Override
    public List<Identity> getIdentities() {
        return mIdentities;
    }

    @Override
    public boolean isUserLoggedIn() {
        if (mCurrentUser == null) {
            mView.startLoginActivity();
            return false;
        }

        return true;
    }

    @Override
    public void onLoginSuccessful() {
        updateCurrentUserAndIdentity();
    }

    @Override
    public void onLogout() {
        updateCurrentUserAndIdentity();
        mView.startHomeActivityAndFinish();
    }

    @Override
    public void onProfileUpdated() {
        notifyPropertyChanged(BR.identityNickname);
        notifyPropertyChanged(BR.identityAvatar);
        mView.showMessage(R.string.toast_profile_update);
    }

    @Override
    public void onIdentityChanged() {
        mIdentities.clear();
        final List<Identity> identities = mCurrentUser.getIdentities();
        if (!identities.isEmpty()) {
            for (Identity identity : identities) {
                if (identity.isActive()) {
                    mIdentities.add(identity);
                }
            }
        }
        mView.notifyHeaderIdentitiesChanged();
        onSettingsIdentitySelected();
    }

    @Override
    public void onSettingsIdentitySelected() {
        mCurrentIdentity = mCurrentUser.getCurrentIdentity();
        notifyPropertyChanged(BR.selectedIdentity);

        mView.onIdentitySelected();
    }

    @Override
    public void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        if (mCurrentIdentity.getObjectId().equals(identity.getObjectId())) {
            return;
        }

        mCurrentIdentity = identity;
        mCurrentUser.setCurrentIdentity(identity);
        mCurrentUser.saveEventually();

        mView.onIdentitySelected();
    }

    @Override
    public void onAvatarClick(View view) {
        mView.startProfileSettingsActivity();
    }
}
