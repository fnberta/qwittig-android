package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileContract.Result;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Created by fabio on 30.09.16.
 */

public class SettingsProfilePresenter extends BasePresenterImpl<SettingsProfileContract.ViewListener>
        implements SettingsProfileContract.Presenter {

    private static final String STATE_VIEW_MODEL = SettingsProfileViewModel.class.getCanonicalName();
    private final SettingsProfileViewModel viewModel;
    private final List<String> groupNicknames;
    private final GroupRepository groupRepo;
    private String avatarOrig;
    private String emailOrig;
    private String nicknameOrig;

    public SettingsProfilePresenter(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull UserRepository userRepo,
                                    @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, userRepo);

        this.groupRepo = groupRepo;
        groupNicknames = new ArrayList<>();

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new SettingsProfileViewModel();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public SettingsProfileViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public boolean showDeleteAvatar() {
        return !TextUtils.isEmpty(viewModel.getAvatar());
    }

    @Override
    public boolean showUnlinkFacebook() {
        return viewModel.isFacebookUser() && !viewModel.isUnlinkSocialLogin();
    }

    @Override
    public boolean showUnlinkGoogle() {
        return viewModel.isGoogleUser() && !viewModel.isUnlinkSocialLogin();
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        viewModel.setGoogleUser(userRepo.isGoogleUser(currentUser));
        viewModel.setFacebookUser(userRepo.isFacebookUser(currentUser));
        view.reloadOptionsMenu();

        emailOrig = currentUser.getEmail();
        if (TextUtils.isEmpty(viewModel.email.get()) && !TextUtils.isEmpty(emailOrig)) {
            viewModel.email.set(emailOrig);
        }

        subscriptions.add(userRepo.getUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> {
                    final String nickname = identity.getNickname();
                    nicknameOrig = nickname;
                    if (TextUtils.isEmpty(viewModel.nickname.get())) {
                        viewModel.nickname.set(nickname);
                    }

                    final String avatar = identity.getAvatar();
                    avatarOrig = avatar;
                    if (TextUtils.isEmpty(viewModel.getAvatar())) {
                        viewModel.setAvatar(avatar);
                    }
                })
                .flatMapObservable(identity -> groupRepo.getGroupIdentities(identity.getGroup(), true))
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupNicknames.add(identity.getNickname());
                    }
                })
        );
    }

    @Override
    public void onPickAvatarMenuClick() {
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatar) {
        viewModel.setAvatar(avatar);
        view.reloadOptionsMenu();
    }

    @Override
    public void onAvatarLoaded() {
        view.startPostponedEnterTransition();
    }

    @Override
    public void onDeleteAvatarMenuClick() {
        viewModel.setAvatar("");
        view.reloadOptionsMenu();
    }

    @Override
    public void onUnlinkThirdPartyLoginMenuClick() {
        viewModel.setUnlinkSocialLogin(true);
        view.reloadOptionsMenu();
        view.showSetPasswordMessage(R.string.toast_unlink_password_required);
    }

    @Override
    public void onPasswordChanged(CharSequence s, int start, int before, int count) {
        final String password = s.toString();
        viewModel.setPassword(password);

        if (viewModel.isUnlinkSocialLogin()) {
            if (TextUtils.isEmpty(password)) {
                view.showSetPasswordMessage(R.string.toast_unlink_password_required);
            } else {
                view.dismissSetPasswordMessage();
            }
        }
    }

    @Override
    public void onSaveProfileClick(View view) {
        if (!viewModel.isInputValid()) {
            return;
        }

        final String nickname = viewModel.nickname.get();
        if (!Objects.equals(nickname, nicknameOrig) && groupNicknames.contains(nickname)) {
            this.view.showMessage(R.string.toast_profile_nickname_taken);
            return;
        }

        if (viewModel.isUnlinkSocialLogin()) {
            unlinkSocialLogin();
            return;
        }

        final boolean googleUser = viewModel.isGoogleUser();
        final boolean facebookUser = viewModel.isFacebookUser();
        final String email = googleUser || facebookUser || Objects.equals(viewModel.email.get(), emailOrig)
                             ? null
                             : viewModel.email.get();
        final String password = googleUser || facebookUser ? null : viewModel.getPassword();
        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
            this.view.showReAuthenticateDialog(emailOrig);
            return;
        }

        // only nickname and/or avatar changed, proceed straight to saving profile
        saveProfile();
    }

    private void unlinkSocialLogin() {
        if (viewModel.isGoogleUser()) {
            view.reAuthenticateGoogle();
        } else if (viewModel.isFacebookUser()) {
            view.reAuthenticateFacebook();
        }
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        view.showProgressDialog(R.string.progress_profile_unlink);
        view.loadUnlinkGoogleWorker(viewModel.email.get(), viewModel.getPassword(), idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        view.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        subscriptions.add(single
                .flatMap(aVoid -> userRepo.updateProfile(viewModel.nickname.get(),
                        viewModel.getAvatar(), isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onFacebookSignedIn(@NonNull String token) {
        view.showProgressDialog(R.string.progress_profile_unlink);
        view.loadUnlinkFacebookWorker(viewModel.email.get(), viewModel.getPassword(), token);
    }

    @Override
    public void onFacebookLoginFailed() {
        view.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        subscriptions.add(single
                .flatMap(aVoid -> userRepo.updateProfile(viewModel.nickname.get(),
                        viewModel.getAvatar(), isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        view.showProgressDialog(R.string.progress_profile_change_email_pw);
        view.loadChangeEmailPasswordWorker(email, password, viewModel.email.get(),
                viewModel.getPassword());
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        subscriptions.add(single
                .flatMap(aVoid -> userRepo.updateProfile(viewModel.nickname.get(),
                        viewModel.getAvatar(), isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @NonNull
    private SingleSubscriber<User> profileSubscriber(@NonNull final String workerTag) {
        return new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User user) {
                view.removeWorker(workerTag);
                view.hideProgressDialog();

                navigator.finish(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable error) {
                view.removeWorker(workerTag);
                view.hideProgressDialog();

                Timber.e(error, "failed to unlink or change email/pw and save profile with error:");
                view.showMessage(R.string.toast_error_profile);
            }
        };
    }

    private void saveProfile() {
        subscriptions.add(userRepo.updateProfile(viewModel.nickname.get(), viewModel.getAvatar(), isAvatarChanged())
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        navigator.finish(Activity.RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        view.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            view.showDiscardChangesDialog();
        } else {
            navigator.finish(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        return !Objects.equals(viewModel.email.get(), emailOrig)
                || !Objects.equals(viewModel.nickname.get(), nicknameOrig)
                || isAvatarChanged()
                || !TextUtils.isEmpty(viewModel.getPassword());
    }

    private boolean isAvatarChanged() {
        return !Objects.equals(viewModel.getAvatar(), avatarOrig);
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(Result.CHANGES_DISCARDED);
    }
}
