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

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.SingleSubscriber;

/**
 * Created by fabio on 12.01.16.
 */
public class NavDrawerViewModelImpl extends ViewModelBaseImpl<NavDrawerViewModel.ViewListener>
        implements NavDrawerViewModel {

    private IdentityRepository mIdentityRepo;
    private List<ParseObject> mUserIdentities;

    public NavDrawerViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull NavDrawerViewModel.ViewListener view,
                                  @NonNull UserRepository userRepository,
                                  @NonNull IdentityRepository identityRepository) {
        super(savedState, view, userRepository);

        mIdentityRepo = identityRepository;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isUserLoggedIn()) {
            loadIdentities();
        }
    }

    private void loadIdentities() {
        mUserIdentities = mCurrentUser.getIdentities();
        mSubscriptions.add(mIdentityRepo.fetchIdentitiesDataAsync(mUserIdentities)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        mView.bindHeaderView();
                        mView.setupHeaderIdentitySelection(identities);
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                })
        );
    }

    @Override
    @Bindable
    public String getIdentityNickname() {
        return mCurrentIdentity.getNickname();
    }

    @Override
    @Bindable
    public byte[] getIdentityAvatar() {
        return mCurrentIdentity.getAvatar();
    }

    @Override
    @Bindable
    public int getSelectedIdentity() {
        return mUserIdentities.indexOf(mCurrentIdentity);
    }

    @Override
    public void notifySelectedGroupChanged() {
        notifyPropertyChanged(BR.selectedIdentity);
    }

    @Override
    public boolean isUserLoggedIn() {
        return mCurrentUser != null;
    }

    @Override
    public void onLoginSuccessful() {
        updateCurrentUserIdentityGroup();
    }

    @Override
    public void onLogout() {
        mView.startHomeActivityAndFinish();
    }

    @Override
    public void onIdentityChanged() {
        updateIdentitySelectionList();
        notifySelectedGroupChanged();
    }

    private void updateIdentitySelectionList() {
        mUserIdentities.clear();
        List<ParseObject> identities = mCurrentUser.getIdentities();
        if (!identities.isEmpty()) {
            mUserIdentities.addAll(identities);
        }

        mView.notifyHeaderIdentitiesChanged();
    }

    @Override
    public void onProfileUpdated() {
        notifyPropertyChanged(BR.userNickname);
        notifyPropertyChanged(BR.userAvatar);
        mView.showMessage(R.string.toast_profile_update);
    }

    @Override
    public void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        if (mCurrentIdentity.getObjectId().equals(identity.getObjectId())) {
            return;
        }

        mCurrentUser.setCurrentIdentity(identity);
        mCurrentUser.saveEventually();
        mView.onIdentitySelected();
    }

    @Override
    public void onAvatarClick(View view) {
        mView.startProfileSettingsActivity();
    }
}
