package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 01.05.16.
 */
public class LoginProfileViewModelImpl extends ViewModelBaseImpl<LoginProfileViewModel.ViewListener>
        implements LoginProfileViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_AVATAR = "STATE_AVATAR";
    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private String mAvatar;
    private String mNickname;
    private boolean mValidate;

    public LoginProfileViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull LoginProfileViewModel.ViewListener view,
                                     @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mAvatar = savedState.getString(STATE_AVATAR);
            mNickname = savedState.getString(STATE_NICKNAME);
        } else {
            mAvatar = mCurrentIdentity.getAvatarUrl();
            mNickname = mCurrentIdentity.getNickname();
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
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    @Override
    public void onAvatarClick(View view) {
        mView.showAvatarPicker();
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

        final boolean avatarSet = !TextUtils.isEmpty(mAvatar) && !mAvatar.equals(mCurrentIdentity.getAvatarUrl());
        if (avatarSet) {
            getSubscriptions().add(mView.encodeAvatar(mAvatar)
                    .flatMapObservable(new Func1<byte[], Observable<Identity>>() {
                        @Override
                        public Observable<Identity> call(byte[] avatar) {
                            return mUserRepo.saveCurrentUserProfile(mNickname, avatar);
                        }
                    })
                    .subscribe(new Subscriber<Identity>() {
                        @Override
                        public void onCompleted() {
                            // TODO: add real check if it's default group or not
                            if (mCurrentIdentity.getGroup().getName().equals("Qwittig")) {
                                mView.showFirstGroupFragment();
                            } else {
                                mView.finishScreen(Activity.RESULT_OK);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO: handle error
                        }

                        @Override
                        public void onNext(Identity identity) {
                            // do nothing
                        }
                    })
            );
        } else {
            mCurrentIdentity.setNickname(mNickname);
            mCurrentIdentity.saveEventually();
            mView.showFirstGroupFragment();
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }
}
