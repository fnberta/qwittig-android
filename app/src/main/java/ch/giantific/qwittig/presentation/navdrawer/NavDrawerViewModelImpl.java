/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.SpinnerInteraction;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link NavDrawerViewModel}.
 */
public class NavDrawerViewModelImpl extends ViewModelBaseImpl<NavDrawerViewModel.ViewListener>
        implements NavDrawerViewModel {

    private final List<Identity> mIdentities;
    private SpinnerInteraction mSpinnerInteraction;
    private Identity mCurrentIdentity;
    private String mNickname;
    private String mAvatar;

    public NavDrawerViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mIdentities = new ArrayList<>();
    }

    @Override
    public void setSpinnerInteraction(@NonNull SpinnerInteraction spinnerInteraction) {
        mSpinnerInteraction = spinnerInteraction;
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
    public int getSelectedIdentity() {
        for (int i = 0, mIdentitiesSize = mIdentities.size(); i < mIdentitiesSize; i++) {
            final Identity identity = mIdentities.get(i);
            if (Objects.equals(mCurrentIdentity.getId(), identity.getId())) {
                return i;
            }
        }

        return 0;
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<User>>() {
                    @Override
                    public Observable<User> call(final User user) {
                        return mUserRepo.observeIdentity(user.getCurrentIdentity())
                                .doOnNext(new Action1<Identity>() {
                                    @Override
                                    public void call(Identity identity) {
                                        mCurrentIdentity = identity;
                                        setNickname(identity.getNickname());
                                        setAvatar(identity.getAvatar());
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
                                    public Observable<Identity> call(String identityId) {
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
                        mIdentities.clear();
                        mIdentities.addAll(identities);
                        mSpinnerInteraction.notifyDataSetChanged();
                        notifyPropertyChanged(BR.selectedIdentity);
                    }
                })
        );
    }

    @Override
    public List<Identity> getIdentities() {
        return mIdentities;
    }

    @Override
    public boolean isUserLoggedIn() {
        if (mUserRepo.getCurrentUser() == null) {
            mNavigator.startLogin();
            return false;
        }

        return true;
    }

    @Override
    public void afterLogout() {
        mNavigator.startHome();
        mNavigator.finish();
    }

    @Override
    public void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        final String identityId = identity.getId();
        if (Objects.equals(mCurrentIdentity.getId(), identityId)) {
            return;
        }

        mUserRepo.updateCurrentIdentity(mCurrentIdentity.getUser(), identityId);
    }

    @Override
    public void onAvatarClick(View view) {
        mNavigator.startProfileSettings();
    }
}
