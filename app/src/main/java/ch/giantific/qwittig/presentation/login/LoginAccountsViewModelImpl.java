/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link LoginAccountsViewModel}.
 */
public class LoginAccountsViewModelImpl extends ViewModelBaseImpl<LoginAccountsViewModel.ViewListener>
        implements LoginAccountsViewModel {

    private static final String STATE_IDENTITY_ID = "STATE_IDENTITY_ID";
    private final RemoteConfigHelper mConfigHelper;
    private final GroupRepository mGroupRepo;
    private String mIdentityId;

    public LoginAccountsViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull RemoteConfigHelper configHelper,
                                      @NonNull UserRepository userRepository,
                                      @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mConfigHelper = configHelper;
        mGroupRepo = groupRepository;

        if (savedState != null) {
            mIdentityId = savedState.getString(STATE_IDENTITY_ID, "");
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(mIdentityId)) {
            outState.putString(STATE_IDENTITY_ID, mIdentityId);
        }
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> single,
                                   @NonNull final String workerTag,
                                   @LoginWorker.Type int type) {
        getSubscriptions().add(single
                .flatMap(new Func1<FirebaseUser, Single<Boolean>>() {
                    @Override
                    public Single<Boolean> call(final FirebaseUser firebaseUser) {
                        final String userId = firebaseUser.getUid();
                        if (!TextUtils.isEmpty(mIdentityId)) {
                            return mUserRepo.getIdentity(mIdentityId)
                                    .doOnSuccess(new Action1<Identity>() {
                                        @Override
                                        public void call(Identity identity) {
                                            mGroupRepo.joinGroup(userId, mIdentityId, identity.getGroup());
                                        }
                                    })
                                    .flatMap(new Func1<Identity, Single<? extends Boolean>>() {
                                        @Override
                                        public Single<? extends Boolean> call(Identity identity) {
                                            return mUserRepo.isUserNew(userId);
                                        }
                                    });
                        }

                        return mUserRepo.isUserNew(userId)
                                .doOnSuccess(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean isUserNew) {
                                        if (isUserNew) {
                                            createInitialGroup(firebaseUser, userId);
                                        }
                                    }
                                });
                    }
                })
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isUserNew) {
                        mView.removeWorker(workerTag);

                        if (isUserNew) {
                            mView.showProfileFragment(!TextUtils.isEmpty(mIdentityId));
                        } else {
                            mNavigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

                        mView.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    private void createInitialGroup(@NonNull FirebaseUser firebaseUser,
                                    @NonNull String userId) {
        String nickname = "";
        String avatar = "";
        final List<? extends UserInfo> userInfos = firebaseUser.getProviderData();
        if (!userInfos.isEmpty()) {
            final UserInfo userInfo = userInfos.get(0);
            nickname = userInfo.getDisplayName();
            final Uri uri = userInfo.getPhotoUrl();
            if (uri != null) {
                avatar = uri.toString();
            }
        }

        if (TextUtils.isEmpty(nickname)) {
            final String email = firebaseUser.getEmail();
            nickname = !TextUtils.isEmpty(email)
                    ? email.substring(0, email.indexOf("@"))
                    : "";
        }
        mGroupRepo.createGroup(userId,
                mConfigHelper.getDefaultGroupName(),
                mConfigHelper.getDefaultGroupCurrency(),
                nickname, avatar);
    }

    @Override
    public void setInvitationIdentityId(@NonNull String identityId) {
        mIdentityId = identityId;
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        mView.loadGoogleLoginWorker(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        setLoading(false);
        mView.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void onFacebookSignedIn(@NonNull String idToken) {
        setLoading(true);
        mView.loadFacebookLoginWorker(idToken);
    }

    @Override
    public void onFacebookLoginFailed() {
        setLoading(false);
        mView.showMessage(R.string.toast_login_failed_facebook);
    }

    @Override
    public View.OnClickListener getLoginGoogleClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                mView.loginWithGoogle();
            }
        };
    }

    @Override
    public void onUseEmailClick(View view) {
        mView.showEmailFragment(mIdentityId);
    }
}
