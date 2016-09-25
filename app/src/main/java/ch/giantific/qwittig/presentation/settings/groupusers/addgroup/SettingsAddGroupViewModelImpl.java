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
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;

/**
 * Provides an implementation of the {@link SettingsAddGroupViewModel}.
 */
public class SettingsAddGroupViewModelImpl extends ViewModelBaseImpl<SettingsAddGroupViewModel.ViewListener>
        implements SettingsAddGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";

    private final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    private final List<String> groupNames;
    private Identity currentIdentity;
    private String name;
    private String currency;
    private boolean validate;

    public SettingsAddGroupViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull Navigator navigator,
                                         @NonNull RxBus<Object> eventBus,
                                         @NonNull RemoteConfigHelper configHelper,
                                         @NonNull UserRepository userRepo,
                                         @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.configHelper = configHelper;
        this.groupRepo = groupRepo;
        groupNames = new ArrayList<>();

        if (savedState != null) {
            validate = savedState.getBoolean(STATE_VALIDATE);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, validate);
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
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(name);
    }

    @Override
    public void onNameChanged(CharSequence s, int start, int before, int count) {
        name = s.toString();
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return configHelper.getSupportedCurrencies();
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId)
                        .doOnSuccess(identity -> currentIdentity = identity)
                        .flatMap(identity -> userRepo.getUser(identity.getUser()))
                        .flatMapObservable(user -> Observable.from(user.getIdentitiesIds())))
                .flatMap(userRepo::observeIdentity)
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupNames.add(identity.getGroupName());
                    }
                })
        );
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        this.currency = currency.getCode();
    }

    @Override
    public void onFabCreateClick(View view) {
        setValidate(true);
        if (!isNameComplete()) {
            return;
        }

        boolean newGroup = true;
        for (String groupName : groupNames) {
            if (name.equalsIgnoreCase(groupName)) {
                newGroup = false;
            }
        }
        if (newGroup) {
            groupRepo.createGroup(currentIdentity.getUser(), name, currency,
                    currentIdentity.getNickname(), currentIdentity.getAvatar());
            this.view.setScreenResult(name);
            this.view.showAddUsersFragment();
        } else {
            this.view.showMessage(R.string.toast_group_already_in_list);
        }
    }
}
