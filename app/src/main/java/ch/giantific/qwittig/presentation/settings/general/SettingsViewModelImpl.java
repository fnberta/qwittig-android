/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseObject;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link SettingsViewModel}.
 */
public class SettingsViewModelImpl extends ViewModelBaseImpl<SettingsViewModel.ViewListener>
        implements SettingsViewModel {

    public SettingsViewModelImpl(@Nullable Bundle savedState,
                                 @NonNull SettingsViewModel.ViewListener view,
                                 @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);
    }

    @Override
    public void onPreferencesLoaded() {
        setupCurrentGroupCategory();
        loadIdentitySelection();
    }

    private void setupCurrentGroupCategory() {
        final String groupName = mCurrentIdentity.getGroup().getName();
        mView.setCurrentGroupTitle(groupName);
        mView.setChangeGroupNameText(groupName);
        mView.setLeaveGroupTitle(R.string.pref_group_leave_group, groupName);
    }

    private void loadIdentitySelection() {
        final List<Identity> identities = mCurrentUser.getIdentities();
        final List<String> identityEntries = new ArrayList<>();
        final List<String> identityValues = new ArrayList<>();
        for (Identity identity : identities) {
            identityEntries.add(identity.getGroup().getName());
            identityValues.add(identity.getObjectId());
        }

        final int size = identityEntries.size();
        final CharSequence[] entries = identityEntries.toArray(new CharSequence[size]);
        final CharSequence[] values = identityValues.toArray(new CharSequence[size]);
        final String selectedValue = mCurrentIdentity.getObjectId();

        mView.setupGroupSelection(entries, values, selectedValue);
    }


    @Override
    public void onGroupSelected(@NonNull String identityId) {
        if (TextUtils.isEmpty(identityId)) {
            return;
        }

        final Identity selectedIdentity = (Identity) ParseObject.createWithoutData(Identity.CLASS, identityId);
        mCurrentUser.setCurrentIdentity(selectedIdentity);
        mCurrentUser.saveEventually();
        mCurrentIdentity = selectedIdentity;

        setupCurrentGroupCategory();

        // new group selected, tell calling activity to reload data
        mView.setResult(Result.GROUP_SELECTED);
    }

    @Override
    public void onGroupNameChanged(@NonNull String newName) {
        final Group group = mCurrentIdentity.getGroup();
        if (!TextUtils.isEmpty(newName) && !group.getName().equals(newName)) {
            group.setName(newName);
            group.saveEventually();

            setupCurrentGroupCategory();
            loadIdentitySelection();

            // NavDrawer group setting needs to be updated
            mView.setResult(Result.GROUP_CHANGED);
        }
    }

    @Override
    public void onLeaveGroupClick() {
        if (!BigFraction.ZERO.equals(mCurrentIdentity.getBalance())) {
            mView.showMessage(R.string.toast_leave_group_balance_not_zero);
            return;
        }

        if (mCurrentUser.getIdentities().size() < 2) {
            mView.showMessage(R.string.toast_settings_min_one_group);
            return;
        }

        getSubscriptions().add(mUserRepo.getIdentities(mCurrentIdentity.getGroup(), false)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        final int message = identities.size() == 1 &&
                                identities.get(0).getObjectId().equals(mCurrentIdentity.getObjectId())
                                ? R.string.dialog_group_leave_delete_message
                                : R.string.dialog_group_leave_message;
                        mView.showLeaveGroupDialog(message);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_unknown);
                    }
                })
        );
    }

    @Override
    public void onLeaveGroupSelected() {
        mUserRepo.unSubscribeGroup(mCurrentIdentity.getGroup());
        mCurrentIdentity = mCurrentUser.archiveCurrentIdentity();
        mCurrentUser.saveEventually();

        loadIdentitySelection();

        // NavDrawer group setting needs to be updated
        mView.setResult(Result.GROUP_CHANGED);
    }

    @Override
    public void onGroupAdded(@NonNull String groupName) {
        mView.showMessage(R.string.toast_group_added, groupName);
        setupCurrentGroupCategory();
        loadIdentitySelection();

        // NavDrawer group setting needs to be updated
        mView.setResult(Result.GROUP_CHANGED);
    }

    @Override
    public void onLogoutMenuClick() {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        mView.showProgressDialog(R.string.progress_logout);
        mView.loadLogoutWorker(false);
    }

    @Override
    public void onDeleteAccountMenuClick() {
        mView.showDeleteAccountDialog();
    }

    @Override
    public void onDeleteAccountSelected() {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        mView.showProgressDialog(R.string.progress_account_delete);
        mView.loadLogoutWorker(true);
    }

    @Override
    public void setLogoutStream(@NonNull Single<User> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User value) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.setResult(Result.LOGOUT);
                mView.finishScreen();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();
                mView.showMessage(mUserRepo.getErrorMessage(error));
            }
        }));
    }
}
