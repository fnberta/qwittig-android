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

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.users.itemmodels.SettingsUsersUserItemModel;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsUsersViewModel}.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersUserItemModel, SettingsUsersViewModel.ViewListener>
        implements SettingsUsersViewModel {

    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_AVATAR_IDENTITY_ID = "STATE_AVATAR_IDENTITY_ID";
    private final GroupRepository mGroupRepo;
    private Identity mCurrentIdentity;
    private String mGroupName;
    private String mNickname;
    private boolean mValidate;
    private int mAvatarIdentityPos;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepository,
                                      @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mGroupRepo = groupRepository;

        if (savedState != null) {
            mNickname = savedState.getString(STATE_NICKNAME, "");
            mValidate = savedState.getBoolean(STATE_VALIDATE, false);
            mAvatarIdentityPos = savedState.getInt(STATE_AVATAR_IDENTITY_ID);
        }
    }

    @Override
    protected Class<SettingsUsersUserItemModel> getItemModelClass() {
        return SettingsUsersUserItemModel.class;
    }

    @Override
    protected int compareItemModels(SettingsUsersUserItemModel o1, SettingsUsersUserItemModel o2) {
        return o1.compareTo(o2);
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
        return mGroupName;
    }

    @Override
    public void setGroupName(@NonNull String groupName) {
        mGroupName = groupName;
        notifyPropertyChanged(BR.groupName);
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
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .doOnNext(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        mCurrentIdentity = identity;
                        setGroupName(identity.getGroupName());
                        mItems.clear();
                    }
                })
                .flatMap(new Func1<Identity, Observable<RxChildEvent<Identity>>>() {
                    @Override
                    public Observable<RxChildEvent<Identity>> call(Identity identity) {
                        return mUserRepo.observeGroupIdentityChildren(identity.getGroup());
                    }
                })
                .map(new Func1<RxChildEvent<Identity>, SettingsUsersUserItemModel>() {
                    @Override
                    public SettingsUsersUserItemModel call(RxChildEvent<Identity> event) {
                        return new SettingsUsersUserItemModel(event.getEventType(),
                                event.getValue());
                    }
                })
                .subscribe(this)
        );
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        mView.showMessage(R.string.toast_error_users_load);
    }

    @Override
    public void onInviteClick(int position) {
        final SettingsUsersUserItemModel userItem = getItemAtPosition(position);
        mView.loadLinkShareOptions(userItem.getInvitationLink());
    }

    @Override
    public void onEditNicknameClick(int position) {
        final SettingsUsersUserItemModel userItem = getItemAtPosition(position);
        mView.showChangeNicknameDialog(userItem.getNickname(), position);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        final SettingsUsersUserItemModel itemModel = getItemAtPosition(position);
        mUserRepo.updatePendingIdentityNickname(itemModel.getId(), nickname);
    }

    @Override
    public void onEditAvatarClick(int position) {
        mAvatarIdentityPos = position;
        mNavigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        final SettingsUsersUserItemModel itemModel = getItemAtPosition(mAvatarIdentityPos);
        mUserRepo.updatePendingIdentityAvatar(itemModel.getId(), avatarPath);
    }

    @Override
    public void onRemoveClick(final int position) {
        final SettingsUsersUserItemModel itemModel = getItemAtPosition(position);
        if (!Objects.equals(itemModel.getBalance(), BigFraction.ZERO)) {
            mView.showMessage(R.string.toast_del_identity_balance_not_zero);
            return;
        }

        mUserRepo.removePendingIdentity(itemModel.getId());
        mView.showMessage(R.string.toast_settings_users_removed);
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
            mGroupRepo.addIdentityToGroup(mCurrentIdentity, mNickname);
            setValidate(false);
            setNickname("");
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }
}
