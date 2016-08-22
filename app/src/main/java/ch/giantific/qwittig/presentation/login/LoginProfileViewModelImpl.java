package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.SingleSubscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by fabio on 01.05.16.
 */
public class LoginProfileViewModelImpl extends ViewModelBaseImpl<LoginProfileViewModel.ViewListener>
        implements LoginProfileViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_AVATAR = "STATE_AVATAR";
    private static final String STATE_NICKNAME = "STATE_NICKNAME";

    private boolean withInvitation;
    private String avatar;
    private String nickname;
    private boolean validate;

    public LoginProfileViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepo) {
        super(savedState, navigator, eventBus, userRepo);

        if (savedState != null) {
            validate = savedState.getBoolean(STATE_VALIDATE);
            avatar = savedState.getString(STATE_AVATAR);
            nickname = savedState.getString(STATE_NICKNAME);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, validate);
        outState.putString(STATE_AVATAR, avatar);
        outState.putString(STATE_NICKNAME, nickname);
    }

    @Override
    public void setWithInvitation(boolean withInvitation) {
        this.withInvitation = withInvitation;
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return validate;
    }

    @Override
    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(@NonNull String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Override
    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        nickname = s.toString();
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        if (TextUtils.isEmpty(nickname)) {
                            setNickname(identity.getNickname());
                        }

                        if (TextUtils.isEmpty(avatar)) {
                            setAvatar(identity.getAvatar());
                        }
                    }
                })
        );
    }

    @Override
    public void onAvatarClick(View view) {
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        setAvatar(avatarPath);
    }

    @Override
    public void onFabDoneClick(View view) {
        if (!validate()) {
            return;
        }

        getSubscriptions().add(userRepo.updateProfile(nickname, avatar, true)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (withInvitation) {
                            navigator.finish(Activity.RESULT_OK);
                        } else {
                            LoginProfileViewModelImpl.this.view.showFirstGroupScreen();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        LoginProfileViewModelImpl.this.view.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }
}
