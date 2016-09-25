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
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.SpinnerInteraction;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;

/**
 * Provides an implementation of the {@link NavDrawerViewModel}.
 */
public class NavDrawerViewModelImpl extends ViewModelBaseImpl<NavDrawerViewModel.ViewListener>
        implements NavDrawerViewModel {

    private final List<Identity> identities;
    private SpinnerInteraction spinnerInteraction;
    private Identity currentIdentity;
    private String nickname;
    private String avatar;

    public NavDrawerViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        identities = new ArrayList<>();
    }

    @Override
    public void setSpinnerInteraction(@NonNull SpinnerInteraction spinnerInteraction) {
        this.spinnerInteraction = spinnerInteraction;
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
    public int getSelectedIdentity() {
        for (int i = 0, mIdentitiesSize = identities.size(); i < mIdentitiesSize; i++) {
            final Identity identity = identities.get(i);
            if (Objects.equals(currentIdentity.getId(), identity.getId())) {
                return i;
            }
        }

        return 0;
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.observeIdentity(currentIdentityId)
                        .doOnNext(identity -> {
                            currentIdentity = identity;
                            setNickname(identity.getNickname());
                            setAvatar(identity.getAvatar());
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
                        NavDrawerViewModelImpl.this.identities.clear();
                        NavDrawerViewModelImpl.this.identities.addAll(identities);
                        spinnerInteraction.notifyDataSetChanged();
                        notifyPropertyChanged(BR.selectedIdentity);
                    }
                })
        );
    }

    @Override
    public List<Identity> getIdentities() {
        return identities;
    }

    @Override
    public boolean isUserLoggedIn() {
        if (userRepo.getCurrentUser() == null) {
            navigator.startLogin();
            return false;
        }

        return true;
    }

    @Override
    public void afterLogout() {
        navigator.startHome();
        navigator.finish();
    }

    @Override
    public void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        final String identityId = identity.getId();
        if (Objects.equals(currentIdentity.getId(), identityId)) {
            return;
        }

        userRepo.updateCurrentIdentity(currentIdentity.getUser(), identityId);
    }

    @Override
    public void onAvatarClick(View view) {
        navigator.startProfileSettings();
    }
}
