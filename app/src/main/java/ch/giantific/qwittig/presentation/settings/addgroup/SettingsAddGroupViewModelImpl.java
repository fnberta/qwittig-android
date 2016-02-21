/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 07.02.16.
 */
public class SettingsAddGroupViewModelImpl extends ViewModelBaseImpl<SettingsAddGroupViewModel.ViewListener>
        implements SettingsAddGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private String mName;
    private String mCurrency;
    private boolean mValidate;
    private IdentityRepository mIdentityRepo;

    public SettingsAddGroupViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull SettingsAddGroupViewModel.ViewListener view,
                                         @NonNull UserRepository userRepository,
                                         @NonNull IdentityRepository identityRepository) {
        super(savedState, view, userRepository);

        mIdentityRepo = identityRepository;

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

        final List<Identity> identities = mCurrentUser.getIdentities();
        boolean newGroup = true;
        for (Identity identity : identities) {
            if (mName.equalsIgnoreCase(identity.getGroup().getName())) {
                newGroup = false;
            }
        }
        if (newGroup) {
            mView.toggleProgressDialog(true);
            mView.loadAddGroupWorker(mName, mCurrency);
        } else {
            mView.showMessage(R.string.toast_group_already_in_list);
        }
    }

    @Override
    public void setCreateGroupStream(@NonNull Single<User> single, @NonNull final String workerTag) {
        getSubscriptions().add(single
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        mView.showAddUsersFragment();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        // TODO: handle error
                    }
                })
        );
    }
}
