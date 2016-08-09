/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsViewModel}.
 */
public class SettingsViewModelImpl extends ViewModelBaseImpl<SettingsViewModel.ViewListener>
        implements SettingsViewModel {

    private static final String STATE_IDENTITIES_IDS = "STATE_IDENTITIES_IDS";
    private static final String STATE_GROUP_NAMES = "STATE_GROUP_NAMES";
    private static final String STATE_CURRENT_IDENTITY_ID = "STATE_CURRENT_IDENTITY_ID";
    private final GroupRepository mGroupRepo;
    private FirebaseUser mFirebaseUser;
    private Identity mCurrentIdentity;
    private ArrayList<String> mIdentitiesIds;
    private ArrayList<String> mGroupNames;
    private String mCurrentIdentityId;

    public SettingsViewModelImpl(@Nullable Bundle savedState,
                                 @NonNull Navigator navigator,
                                 @NonNull RxBus<Object> eventBus,
                                 @NonNull UserRepository userRepository,
                                 @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mGroupRepo = groupRepository;

        if (savedState != null) {
            mIdentitiesIds = savedState.getStringArrayList(STATE_IDENTITIES_IDS);
            mGroupNames = savedState.getStringArrayList(STATE_GROUP_NAMES);
            mCurrentIdentityId = savedState.getString(STATE_CURRENT_IDENTITY_ID);
        } else {
            mIdentitiesIds = new ArrayList<>();
            mGroupNames = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_IDENTITIES_IDS, mIdentitiesIds);
        outState.putStringArrayList(STATE_GROUP_NAMES, mGroupNames);
        outState.putString(STATE_CURRENT_IDENTITY_ID, mCurrentIdentityId);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        mFirebaseUser = currentUser;
        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<User>>() {
                    @Override
                    public Observable<User> call(final User user) {
                        return mUserRepo.observeIdentity(user.getCurrentIdentity())
                                .doOnNext(new Action1<Identity>() {
                                    @Override
                                    public void call(Identity identity) {
                                        mCurrentIdentity = identity;
                                        mCurrentIdentityId = identity.getId();
                                        setupCurrentGroupCategory();
                                    }
                                })
                                .map(new Func1<Identity, User>() {
                                    @Override
                                    public User call(Identity identity) {
                                        return user;
                                    }
                                });
                    }
                })
                .flatMap(new Func1<User, Observable<List<Identity>>>() {
                    @Override
                    public Observable<List<Identity>> call(User user) {
                        return Observable.from(user.getIdentitiesIds())
                                .flatMap(new Func1<String, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(final String identityId) {
                                        return mUserRepo.getIdentity(identityId).toObservable();
                                    }
                                })
                                .toList();
                    }
                })
                .subscribe(new IndefiniteSubscriber<List<Identity>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        mView.showMessage(R.string.toast_error_load_groups);
                    }

                    @Override
                    public void onNext(List<Identity> identities) {
                        mIdentitiesIds.clear();
                        mGroupNames.clear();
                        for (Identity identity : identities) {
                            final String identityId = identity.getId();
                            mIdentitiesIds.add(identityId);
                            final String groupName = identity.getGroupName();
                            mGroupNames.add(groupName);
                        }
                        setIdentitySelection();
                    }
                })
        );
    }

    @Override
    protected void onUserNotLoggedIn() {
        super.onUserNotLoggedIn();

        mNavigator.finish(Result.LOGOUT);
    }

    private void setupCurrentGroupCategory() {
        final String groupName = mCurrentIdentity.getGroupName();
        mView.setCurrentGroupTitle(groupName);
        mView.setChangeGroupNameText(groupName);
        mView.setLeaveGroupTitle(R.string.pref_group_leave_group, groupName);
    }

    @Override
    public void onPreferencesLoaded() {
        if (!mIdentitiesIds.isEmpty() && !mGroupNames.isEmpty()) {
            setIdentitySelection();
        }
    }

    private void setIdentitySelection() {
        final int size = mGroupNames.size();
        final CharSequence[] entries = mGroupNames.toArray(new CharSequence[size]);
        final CharSequence[] values = mIdentitiesIds.toArray(new CharSequence[size]);
        final String selectedValue = mCurrentIdentityId;
        mView.setupGroupSelection(entries, values, selectedValue);
    }

    @Override
    public void onGroupSelected(@NonNull String identityId) {
        if (TextUtils.isEmpty(identityId)) {
            return;
        }

        mUserRepo.updateCurrentIdentity(mCurrentIdentity.getUser(), identityId);
    }

    @Override
    public void onGroupNameChanged(@NonNull final String newName) {
        if (!TextUtils.isEmpty(newName) && !Objects.equals(mCurrentIdentity.getGroupName(), newName)) {
            getSubscriptions().add(mGroupRepo.updateGroupDetails(mCurrentIdentity.getGroup(), newName, null)
                    .subscribe(new SingleSubscriber<Group>() {
                        @Override
                        public void onSuccess(Group group) {
                            mView.showMessage(R.string.toast_group_name_changed, newName);
                        }

                        @Override
                        public void onError(Throwable error) {
                            mView.showMessage(R.string.toast_error_settings_group_name_change);
                        }
                    })
            );
        }
    }

    @Override
    public void onLeaveGroupClick() {
        if (!Objects.equals(BigFraction.ZERO, mCurrentIdentity.getBalanceFraction())) {
            mView.showMessage(R.string.toast_leave_group_balance_not_zero);
            return;
        }

        if (mIdentitiesIds.size() < 2) {
            mView.showMessage(R.string.toast_settings_min_one_group);
            return;
        }

        getSubscriptions().add(mGroupRepo.getGroupIdentities(mCurrentIdentity.getGroup(), false)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        final int message = identities.size() == 1 &&
                                Objects.equals(identities.get(0).getId(), mCurrentIdentity.getId())
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
        getSubscriptions().add(mGroupRepo.leaveGroup(mCurrentIdentity)
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {
                        mView.showMessage(R.string.toast_group_left);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to leave group with error:");
                        mView.showMessage(R.string.toast_error_leave_group);
                    }
                }));
    }

    @Override
    public void onLogoutMenuClick() {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        if (mUserRepo.isGoogleUser(mFirebaseUser)) {
            mView.showProgressDialog(R.string.progress_logout);
            mView.loadGoogleUserWorker();
        } else {
            mUserRepo.signOut(mFirebaseUser);
        }
    }

    @Override
    public void onDeleteAccountMenuClick() {
        if (mUserRepo.isGoogleUser(mFirebaseUser) || mUserRepo.isFacebookUser(mFirebaseUser)) {
            mView.showDeleteAccountDialog();
        } else {
            mView.showEmailReAuthenticateDialog(mFirebaseUser.getEmail());
        }
    }

    @Override
    public void onDeleteAccountSelected() {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        mView.showProgressDialog(R.string.progress_account_delete);
        if (mUserRepo.isGoogleUser(mFirebaseUser)) {
            mView.reAuthenticateGoogle();
        } else if (mUserRepo.isFacebookUser(mFirebaseUser)) {
            mView.reAuthenticateFacebook();
        }
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        mView.loadDeleteGoogleUserWorker(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        mView.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @Override
    public void onFacebookSignedIn(@NonNull String token) {
        mView.loadDeleteFacebookUserWorker(token);
    }

    @Override
    public void onFacebookLoginFailed() {
        mView.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        getSubscriptions().add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        mView.loadDeleteEmailUserWorker(email, password);
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @NonNull
    private SingleSubscriber<Void> signOutSubscriber(@NonNull final String workerTag) {
        return new SingleSubscriber<Void>() {
            @Override
            public void onSuccess(Void value) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();
            }

            @Override
            public void onError(Throwable error) {
                Timber.e(error, "logout or delete failed with error:");
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_error_logout);
            }
        };
    }
}
