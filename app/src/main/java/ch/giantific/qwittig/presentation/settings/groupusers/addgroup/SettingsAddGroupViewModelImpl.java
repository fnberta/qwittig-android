/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsAddGroupViewModel}.
 */
public class SettingsAddGroupViewModelImpl extends ViewModelBaseImpl<SettingsAddGroupViewModel.ViewListener>
        implements SettingsAddGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private final RemoteConfigHelper mConfigHelper;
    private final GroupRepository mGroupRepo;
    private final List<String> mGroupNames;
    private Identity mCurrentIdentity;
    private String mName;
    private String mCurrency;
    private boolean mValidate;

    public SettingsAddGroupViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull Navigator navigator,
                                         @NonNull RxBus<Object> eventBus,
                                         @NonNull RemoteConfigHelper configHelper,
                                         @NonNull UserRepository userRepository,
                                         @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mConfigHelper = configHelper;
        mGroupRepo = groupRepository;
        mGroupNames = new ArrayList<>();

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, mValidate);
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
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(mName);
    }

    @Override
    public void onNameChanged(CharSequence s, int start, int before, int count) {
        mName = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return mConfigHelper.getSupportedCurrencies();
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<String>>() {
                    @Override
                    public Observable<String> call(final User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity())
                                .doOnSuccess(new Action1<Identity>() {
                                    @Override
                                    public void call(Identity identity) {
                                        mCurrentIdentity = identity;
                                    }
                                })
                                .flatMapObservable(new Func1<Identity, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(Identity identity) {
                                        return Observable.from(user.getIdentitiesIds());
                                    }
                                });
                    }
                })
                .flatMap(new Func1<String, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(String identityId) {
                        return mUserRepo.observeIdentity(identityId);
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        mGroupNames.add(identity.getGroupName());
                    }
                })
        );
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        mCurrency = currency.getCode();
    }

    @Override
    public void onFabCreateClick(View view) {
        setValidate(true);
        if (!isNameComplete()) {
            return;
        }

        boolean newGroup = true;
        for (String groupName : mGroupNames) {
            if (mName.equalsIgnoreCase(groupName)) {
                newGroup = false;
            }
        }
        if (newGroup) {
            mGroupRepo.createGroup(mCurrentIdentity.getUser(), mName, mCurrency,
                    mCurrentIdentity.getNickname(), mCurrentIdentity.getAvatar());
            mView.setScreenResult(mName);
            mView.showAddUsersFragment();
        } else {
            mView.showMessage(R.string.toast_group_already_in_list);
        }
    }
}
