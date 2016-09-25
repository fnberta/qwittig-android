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
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsViewModel}.
 */
public class SettingsViewModelImpl extends ViewModelBaseImpl<SettingsViewModel.ViewListener>
        implements SettingsViewModel {

    private static final String STATE_IDENTITIES_IDS = "STATE_IDENTITIES_IDS";
    private static final String STATE_GROUP_NAMES = "STATE_GROUP_NAMES";
    private static final String STATE_CURRENT_IDENTITY_ID = "STATE_CURRENT_IDENTITY_ID";

    private final GroupRepository groupRepo;
    private final ArrayList<String> identityIds;
    private final ArrayList<String> groupNames;
    private FirebaseUser firebaseUser;
    private Identity currentIdentity;
    private String currentIdentityId;

    public SettingsViewModelImpl(@Nullable Bundle savedState,
                                 @NonNull Navigator navigator,
                                 @NonNull RxBus<Object> eventBus,
                                 @NonNull UserRepository userRepo,
                                 @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.groupRepo = groupRepo;

        if (savedState != null) {
            identityIds = savedState.getStringArrayList(STATE_IDENTITIES_IDS);
            groupNames = savedState.getStringArrayList(STATE_GROUP_NAMES);
            currentIdentityId = savedState.getString(STATE_CURRENT_IDENTITY_ID);
        } else {
            identityIds = new ArrayList<>();
            groupNames = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_IDENTITIES_IDS, identityIds);
        outState.putStringArrayList(STATE_GROUP_NAMES, groupNames);
        outState.putString(STATE_CURRENT_IDENTITY_ID, currentIdentityId);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        firebaseUser = currentUser;
        getSubscriptions().add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .doOnNext(currentIdentityId -> this.currentIdentityId = currentIdentityId)
                .flatMap(currentIdentityId -> userRepo.observeIdentity(currentIdentityId)
                        .doOnNext(identity -> {
                            currentIdentity = identity;
                            setupCurrentGroupCategory();
                        }))
                .flatMap(identity -> userRepo.getUser(identity.getUser()).toObservable())
                .flatMap(user -> Observable.from(user.getIdentitiesIds())
                        .flatMap(identityId -> userRepo.getIdentity(identityId).toObservable())
                        .toList())
                .subscribe(new IndefiniteSubscriber<List<Identity>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_load_groups);
                    }

                    @Override
                    public void onNext(List<Identity> identities) {
                        identityIds.clear();
                        groupNames.clear();
                        for (Identity identity : identities) {
                            final String identityId = identity.getId();
                            identityIds.add(identityId);
                            final String groupName = identity.getGroupName();
                            groupNames.add(groupName);
                        }
                        setIdentitySelection();
                    }
                })
        );
    }

    @Override
    protected void onUserNotLoggedIn() {
        super.onUserNotLoggedIn();

        navigator.finish(Result.LOGOUT);
    }

    private void setupCurrentGroupCategory() {
        final String groupName = currentIdentity.getGroupName();
        view.setCurrentGroupTitle(groupName);
        view.setChangeGroupNameText(groupName);
        view.setLeaveGroupTitle(R.string.pref_group_leave_group, groupName);
    }

    @Override
    public void onPreferencesLoaded() {
        if (!identityIds.isEmpty() && !groupNames.isEmpty()) {
            setIdentitySelection();
        }
    }

    private void setIdentitySelection() {
        final int size = groupNames.size();
        final CharSequence[] entries = groupNames.toArray(new CharSequence[size]);
        final CharSequence[] values = identityIds.toArray(new CharSequence[size]);
        final String selectedValue = currentIdentityId;
        view.setupGroupSelection(entries, values, selectedValue);
    }

    @Override
    public void onGroupSelected(@NonNull String identityId) {
        if (TextUtils.isEmpty(identityId)) {
            return;
        }

        userRepo.updateCurrentIdentity(currentIdentity.getUser(), identityId);
    }

    @Override
    public void onGroupNameChanged(@NonNull final String newName) {
        if (!TextUtils.isEmpty(newName) && !Objects.equals(currentIdentity.getGroupName(), newName)) {
            getSubscriptions().add(groupRepo.updateGroupDetails(currentIdentity.getGroup(), newName, null)
                    .subscribe(new SingleSubscriber<Group>() {
                        @Override
                        public void onSuccess(Group group) {
                            view.showMessage(R.string.toast_group_name_changed, newName);
                        }

                        @Override
                        public void onError(Throwable error) {
                            view.showMessage(R.string.toast_error_settings_group_name_change);
                        }
                    })
            );
        }
    }

    @Override
    public void onLeaveGroupClick() {
        if (!Objects.equals(BigFraction.ZERO, currentIdentity.getBalanceFraction())) {
            view.showMessage(R.string.toast_leave_group_balance_not_zero);
            return;
        }

        if (identityIds.size() < 2) {
            view.showMessage(R.string.toast_settings_min_one_group);
            return;
        }

        getSubscriptions().add(groupRepo.getGroupIdentities(currentIdentity.getGroup(), false)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        final int message = identities.size() == 1 &&
                                                    Objects.equals(identities.get(0).getId(), currentIdentity.getId())
                                            ? R.string.dialog_group_leave_delete_message
                                            : R.string.dialog_group_leave_message;
                        view.showLeaveGroupDialog(message);
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.showMessage(R.string.toast_error_unknown);
                    }
                })
        );
    }

    @Override
    public void onLeaveGroupSelected() {
        getSubscriptions().add(groupRepo.leaveGroup(currentIdentity)
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {
                        view.showMessage(R.string.toast_group_left);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to leave group with error:");
                        view.showMessage(R.string.toast_error_leave_group);
                    }
                }));
    }

    @Override
    public void onLogoutMenuClick() {
        if (!view.isNetworkAvailable()) {
            view.showMessage(R.string.toast_no_connection);
            return;
        }

        if (userRepo.isGoogleUser(firebaseUser)) {
            view.showProgressDialog(R.string.progress_logout);
            view.loadGoogleUserWorker();
        } else {
            userRepo.signOut(firebaseUser);
        }
    }

    @Override
    public void onDeleteAccountMenuClick() {
        if (userRepo.isGoogleUser(firebaseUser) || userRepo.isFacebookUser(firebaseUser)) {
            view.showDeleteAccountDialog();
        } else {
            view.showEmailReAuthenticateDialog(firebaseUser.getEmail());
        }
    }

    @Override
    public void onDeleteAccountSelected() {
        if (!view.isNetworkAvailable()) {
            view.showMessage(R.string.toast_no_connection);
            return;
        }

        view.showProgressDialog(R.string.progress_account_delete);
        if (userRepo.isGoogleUser(firebaseUser)) {
            view.reAuthenticateGoogle();
        } else if (userRepo.isFacebookUser(firebaseUser)) {
            view.reAuthenticateFacebook();
        }
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        view.loadDeleteGoogleUserWorker(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        view.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @Override
    public void onFacebookSignedIn(@NonNull String token) {
        view.loadDeleteFacebookUserWorker(token);
    }

    @Override
    public void onFacebookLoginFailed() {
        view.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        getSubscriptions().add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        view.showProgressDialog(R.string.progress_account_delete);
        view.loadDeleteEmailUserWorker(email, password);
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
                view.removeWorker(workerTag);
                view.hideProgressDialog();
            }

            @Override
            public void onError(Throwable error) {
                Timber.e(error, "logout or delete failed with error:");
                view.removeWorker(workerTag);
                view.hideProgressDialog();

                view.showMessage(R.string.toast_error_logout);
            }
        };
    }
}
