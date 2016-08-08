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
import rx.Subscriber;
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
    private boolean mWithInvitation;
    private String mAvatar;
    private String mNickname;
    private boolean mValidate;

    public LoginProfileViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mAvatar = savedState.getString(STATE_AVATAR);
            mNickname = savedState.getString(STATE_NICKNAME);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, mValidate);
        outState.putString(STATE_AVATAR, mAvatar);
        outState.putString(STATE_NICKNAME, mNickname);
    }

    @Override
    public void setWithInvitation(boolean withInvitation) {
        mWithInvitation = withInvitation;
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    @Override
    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Override
    public void setAvatar(@NonNull String avatar) {
        mAvatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Override
    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Override
    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        if (TextUtils.isEmpty(mNickname)) {
                            setNickname(identity.getNickname());
                        }

                        if (TextUtils.isEmpty(mAvatar)) {
                            setAvatar(identity.getAvatar());
                        }
                    }
                })
        );
    }

    @Override
    public void onAvatarClick(View view) {
        mNavigator.startImagePicker();
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

        getSubscriptions().add(mUserRepo.updateProfile(mNickname, mAvatar, true)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (mWithInvitation) {
                            mNavigator.finish(Activity.RESULT_OK);
                        } else {
                            mView.showFirstGroupScreen();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        mView.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }
}
