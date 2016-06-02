/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.users.models.SettingsUsersUserRowViewModel;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsUsersViewModel}.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersUserRowViewModel, SettingsUsersViewModel.ViewListener>
        implements SettingsUsersViewModel {

    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private String mNickname;
    private boolean mValidate;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull SettingsUsersViewModel.ViewListener view,
                                      @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        if (savedState != null) {
            mItems = new ArrayList<>();
            mNickname = savedState.getString(STATE_NICKNAME, "");
            mValidate = savedState.getBoolean(STATE_VALIDATE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putString(STATE_NICKNAME, mNickname);
        outState.putBoolean(STATE_VALIDATE, mValidate);
    }

    @Override
    public String getNickname() {
        return mNickname;
    }

    @Override
    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    public void loadData() {
        final SettingsUsersViewModel listener = this;
        final Group group = mCurrentIdentity.getGroup();
        final String currentId = mCurrentIdentity.getObjectId();
        getSubscriptions().add(mUserRepo.getIdentities(group, true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !identity.getObjectId().equals(currentId);
                    }
                })
                .map(new Func1<Identity, SettingsUsersUserRowViewModel>() {
                    @Override
                    public SettingsUsersUserRowViewModel call(Identity identity) {
                        return new SettingsUsersUserRowViewModel(listener, identity);
                    }
                })
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<SettingsUsersUserRowViewModel>>() {
                    @Override
                    public void onSuccess(List<SettingsUsersUserRowViewModel> items) {
                        mItems.clear();
                        if (!items.isEmpty()) {
                            mItems.addAll(items);
                        }
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_users_load);
                    }
                })
        );
    }

    @Override
    public void onShareClick(@NonNull String shareLink) {
        mView.loadLinkShareOptions(shareLink);
    }

    @Override
    public boolean isItemDismissable(int position) {
        final SettingsUsersUserRowViewModel userItem = mItems.get(position);
        return userItem.isPending() && userItem.getIdentity().getBalance().equals(BigFraction.ZERO);
    }

    @Override
    public void onItemDismiss(final int position) {
        final SettingsUsersUserRowViewModel userItem = mItems.get(position);
        final Identity identity = userItem.getIdentity();
        getSubscriptions().add(mUserRepo.removePendingIdentity(identity)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity value) {
                        mItems.remove(position);
                        mView.notifyItemRemoved(position);
                        mView.showMessage(R.string.toast_settings_users_removed);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_settings_users_remove);
                    }
                })
        );
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onAddUserClick(View view) {
        if (validate()) {
            final Group group = mCurrentIdentity.getGroup();
            mView.toggleProgressDialog(true);
            mView.loadAddUserWorker(mNickname, group.getObjectId(), group.getName());
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }

    @Override
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull final String workerTag) {
        final SettingsUsersViewModel listener = this;
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        mItems.add(new SettingsUsersUserRowViewModel(listener, identity));
                        mView.notifyItemInserted(getLastPosition());

                        setValidate(false);
                        setNickname("");
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);
                        mView.showMessage(R.string.toast_error_settings_users_add);
                    }
                })
        );
    }
}
