/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.general.SettingsContract.Result;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsContract}.
 */
public class SettingsPresenter extends BasePresenterImpl<SettingsContract.ViewListener>
        implements SettingsContract.Presenter {

    private final SettingsViewModel viewModel;
    private final GroupRepository groupRepo;
    private FirebaseUser firebaseUser;
    private Identity currentIdentity;

    @Inject
    public SettingsPresenter(@NonNull Navigator navigator,
                             @NonNull SettingsViewModel viewModel,
                             @NonNull UserRepository userRepo,
                             @NonNull GroupRepository groupRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        firebaseUser = currentUser;
        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .doOnNext(viewModel::setCurrentIdentityId)
                .flatMap(userRepo::observeIdentity)
                .doOnNext(identity -> {
                    currentIdentity = identity;
                    setupCurrentGroupCategory();
                })
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
                        viewModel.setIdentityIdsGroupNames(identities);
                        setIdentitySelection();
                        view.startEnterTransition();
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
        if (!viewModel.getIdentityIds().isEmpty() && !viewModel.getGroupNames().isEmpty()) {
            setIdentitySelection();
        }
    }

    private void setIdentitySelection() {
        final List<String> groupNames = viewModel.getGroupNames();
        final int size = groupNames.size();
        final CharSequence[] entries = groupNames.toArray(new CharSequence[size]);
        final CharSequence[] values = viewModel.getIdentityIds().toArray(new CharSequence[size]);
        view.setupGroupSelection(entries, values, viewModel.getCurrentIdentityId());
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
            subscriptions.add(groupRepo.updateGroupDetails(currentIdentity.getGroup(), newName, null)
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
    public void onProfileClick() {
        navigator.startProfileSettings();
    }

    @Override
    public void onAddGroupClick() {
        navigator.startAddGroupSettings();
    }

    @Override
    public void onUsersClick() {
        navigator.startUsersSettings();
    }

    @Override
    public void onLeaveGroupClick() {
        if (!Objects.equals(BigFraction.ZERO, currentIdentity.getBalanceFraction())) {
            view.showMessage(R.string.toast_leave_group_balance_not_zero);
            return;
        }

        if (viewModel.getIdentityIds().size() < 2) {
            view.showMessage(R.string.toast_settings_min_one_group);
            return;
        }

        subscriptions.add(groupRepo.getGroupIdentities(currentIdentity.getGroup(), false)
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
        subscriptions.add(groupRepo.leaveGroup(currentIdentity)
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
        subscriptions.add(single.subscribe(signOutSubscriber(workerTag)));
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
        subscriptions.add(single.subscribe(signOutSubscriber(workerTag)));
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        view.showProgressDialog(R.string.progress_account_delete);
        view.loadDeleteEmailUserWorker(email, password);
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        subscriptions.add(single.subscribe(signOutSubscriber(workerTag)));
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
