/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.users.models.SettingsUsersUserRowViewModel;
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
    private static final String STATE_AVATAR_IDENTITY_ID = "STATE_AVATAR_IDENTITY_ID";
    private final Navigator mNavigator;
    private String mNickname;
    private boolean mValidate;
    private int mAvatarIdentityPos;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;

        if (savedState != null) {
            mItems = new ArrayList<>();
            mNickname = savedState.getString(STATE_NICKNAME, "");
            mValidate = savedState.getBoolean(STATE_VALIDATE, false);
            mAvatarIdentityPos = savedState.getInt(STATE_AVATAR_IDENTITY_ID);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putString(STATE_NICKNAME, mNickname);
        outState.putBoolean(STATE_VALIDATE, mValidate);
        if (mAvatarIdentityPos > 0) {
            outState.putInt(STATE_AVATAR_IDENTITY_ID, mAvatarIdentityPos);
        }
    }

    @Override
    @Bindable
    public String getGroupName() {
        return mCurrentIdentity.getGroup().getName();
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
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public boolean isGroupEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public void loadData() {
        final Group group = mCurrentIdentity.getGroup();
        final String currentId = mCurrentIdentity.getObjectId();
        getSubscriptions().add(mUserRepo.getIdentities(group, true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !Objects.equals(identity.getObjectId(), currentId);
                    }
                })
                .map(new Func1<Identity, SettingsUsersUserRowViewModel>() {
                    @Override
                    public SettingsUsersUserRowViewModel call(Identity identity) {
                        return new SettingsUsersUserRowViewModel(identity);
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
                        notifyPropertyChanged(BR.groupEmpty);
                        mListInteraction.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_users_load);
                    }
                })
        );
    }

    @Override
    public void onInviteClick(int position) {
        final SettingsUsersUserRowViewModel userItem = getItemAtPosition(position);
        mView.loadLinkShareOptions(userItem.getIdentity().getInvitationLink());
    }

    @Override
    public void onEditNicknameClick(int position) {
        final SettingsUsersUserRowViewModel userItem = getItemAtPosition(position);
        mView.showChangeNicknameDialog(userItem.getNickname(), position);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        final SettingsUsersUserRowViewModel userItem = getItemAtPosition(position);
        final Identity identity = userItem.getIdentity();
        identity.setNickname(nickname);
        identity.saveEventually();
        mListInteraction.notifyItemChanged(position);
    }

    @Override
    public void onEditAvatarClick(int position) {
        mAvatarIdentityPos = position;
        mNavigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        final SettingsUsersUserRowViewModel userItem = getItemAtPosition(mAvatarIdentityPos);
        final Identity identity = userItem.getIdentity();
        getSubscriptions().add(mUserRepo.saveIdentityWithAvatar(identity, null, avatarPath)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity value) {
                        mListInteraction.notifyItemChanged(mAvatarIdentityPos);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }

    @Override
    public void onRemoveClick(final int position) {
        final SettingsUsersUserRowViewModel userItem = getItemAtPosition(position);
        final Identity identity = userItem.getIdentity();
        if (!Objects.equals(identity.getBalance(), BigFraction.ZERO)) {
            mView.showMessage(R.string.toast_del_identity_balance_not_zero);
            return;
        }

        getSubscriptions().add(mUserRepo.removePendingIdentity(identity)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity value) {
                        mItems.remove(position);
                        mListInteraction.notifyItemRemoved(position);
                        notifyPropertyChanged(BR.groupEmpty);
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
            mView.showProgressDialog(R.string.progress_add_user);
            mView.loadAddUserWorker(mNickname, group.getObjectId(), group.getName());
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }

    @Override
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        mView.removeWorker(workerTag);
                        mView.hideProgressDialog();

                        mItems.add(new SettingsUsersUserRowViewModel(identity));
                        notifyPropertyChanged(BR.groupEmpty);
                        mListInteraction.notifyItemInserted(getLastPosition());

                        setValidate(false);
                        setNickname("");
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.hideProgressDialog();
                        mView.showMessage(R.string.toast_error_settings_users_add);
                    }
                })
        );
    }
}
