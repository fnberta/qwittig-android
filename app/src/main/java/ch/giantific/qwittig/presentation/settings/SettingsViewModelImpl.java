/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseObject;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;

/**
 * Created by fabio on 10.02.16.
 */
public class SettingsViewModelImpl extends ViewModelBaseImpl<SettingsViewModel.ViewListener>
        implements SettingsViewModel {

    private GroupRepository mGroupRepo;
    private IdentityRepository mIdentityRepo;

    public SettingsViewModelImpl(@Nullable Bundle savedState,
                                 @NonNull SettingsViewModel.ViewListener view,
                                 @NonNull UserRepository userRepository,
                                 @NonNull GroupRepository groupRepository,
                                 @NonNull IdentityRepository identityRepository) {
        super(savedState, view, userRepository);

        mGroupRepo = groupRepository;
        mIdentityRepo = identityRepository;
    }

    @Override
    public void onStart() {
        super.onStart();

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
        final String selectedValue = mCurrentIdentity.getObjectId();

        mSubscriptions.add(mIdentityRepo.getUserIdentitiesLocalAsync(mCurrentUser)
                .subscribe(new Subscriber<Identity>() {
                    private List<String> identityEntries = new ArrayList<>();
                    private List<String> identityValues = new ArrayList<>();

                    @Override
                    public void onCompleted() {
                        final int size = identityEntries.size();
                        final CharSequence[] entries = identityEntries.toArray(new CharSequence[size]);
                        final CharSequence[] values = identityValues.toArray(new CharSequence[size]);
                        mView.setupGroupSelection(entries, values, selectedValue);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO: handle error
                    }

                    @Override
                    public void onNext(Identity identity) {
                        identityEntries.add(identity.getGroup().getName());
                        identityValues.add(identity.getObjectId());
                    }
                })
        );
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

        // NavDrawer group setting needs to be updated
        mView.setResult(Result.RESULT_GROUP_CHANGED);
    }

    @Override
    public void onGroupNameChanged(@NonNull String newName) {
        final Group group = mCurrentIdentity.getGroup();
        if (!TextUtils.isEmpty(newName) && !group.getName().equals(newName)) {
            group.setName(newName);
            group.saveEventually();

            loadIdentitySelection();
            setupCurrentGroupCategory();
        }
    }

    @Override
    public void onLeaveGroupClick() {
        if (!BigFraction.ZERO.equals(mCurrentIdentity.getBalance())) {
            mView.showMessage(R.string.toast_balance_not_zero);
            return;
        }

        mSubscriptions.add(mIdentityRepo.getIdentitiesLocalAsync(mCurrentIdentity.getGroup())
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
                        // TODO: handle error
                    }
                })
        );
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
    public void onActionConfirmed() {
        mGroupRepo.unsubscribeGroup(mCurrentIdentity.getGroup());
        mCurrentIdentity.setActive(false);
        mCurrentIdentity.saveEventually();

        mSubscriptions.add(mIdentityRepo.getUserIdentitiesLocalAsync(mCurrentUser)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        // reset to first identity in the list;
                        final Identity firstIdentity = identities.get(0);
                        mCurrentUser.setCurrentIdentity(firstIdentity);
                        final String selectedValue = firstIdentity.getObjectId();
                        mCurrentUser.saveEventually();

                        // reset identities list
                        final int size = identities.size();
                        final List<String> identityEntries = new ArrayList<>(size);
                        final List<String> identityValues = new ArrayList<>(size);

                        for (Identity identity : identities) {
                            identityEntries.add(identity.getGroup().getName());
                            identityValues.add(identity.getObjectId());
                        }

                        final CharSequence[] entries = identityEntries.toArray(new CharSequence[size]);
                        final CharSequence[] values = identityValues.toArray(new CharSequence[size]);
                        mView.setupGroupSelection(entries, values, selectedValue);
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                })
        );

        // NavDrawer group setting needs to be updated
        mView.setResult(Result.RESULT_GROUP_CHANGED);
    }

    @Override
    public void setLogoutStream(@NonNull Single<User> single, @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User value) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.setResult(Result.RESULT_LOGOUT);
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
