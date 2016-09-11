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
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.users.itemmodels.SettingsUsersUserItemModel;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsUsersViewModel}.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersUserItemModel, SettingsUsersViewModel.ViewListener>
        implements SettingsUsersViewModel {

    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_AVATAR_IDENTITY_ID = "STATE_AVATAR_IDENTITY_ID";

    private final GroupRepository groupRepo;
    private Identity currentIdentity;
    private String groupName;
    private String nickname;
    private boolean validate;
    private String avatarIdentityId;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepo,
                                      @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.groupRepo = groupRepo;

        if (savedState != null) {
            nickname = savedState.getString(STATE_NICKNAME, "");
            validate = savedState.getBoolean(STATE_VALIDATE, false);
            avatarIdentityId = savedState.getString(STATE_AVATAR_IDENTITY_ID);
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

        outState.putString(STATE_NICKNAME, nickname);
        outState.putBoolean(STATE_VALIDATE, validate);
        if (!TextUtils.isEmpty(avatarIdentityId)) {
            outState.putString(STATE_AVATAR_IDENTITY_ID, avatarIdentityId);
        }
    }

    @Override
    @Bindable
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
        notifyPropertyChanged(BR.groupName);
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
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()).toObservable())
                .doOnNext(identity -> {
                    currentIdentity = identity;
                    setGroupName(identity.getGroupName());
                    items.clear();
                })
                .flatMap(identity -> groupRepo.observeGroupIdentityChildren(identity.getGroup()))
                .map(event -> new SettingsUsersUserItemModel(event.getEventType(), event.getValue()))
                .subscribe(this)
        );
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_users_load);
    }

    @Override
    public void onInviteClick(int position) {
        final SettingsUsersUserItemModel userItem = getItemAtPosition(position);
        view.loadLinkShareOptions(userItem.getInvitationLink());
    }

    @Override
    public void onEditNicknameClick(int position) {
        final SettingsUsersUserItemModel userItem = getItemAtPosition(position);
        view.showChangeNicknameDialog(userItem.getNickname(), position);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        final SettingsUsersUserItemModel itemModel = getItemAtPosition(position);
        userRepo.updatePendingIdentityNickname(itemModel.getId(), nickname);
    }

    @Override
    public void onEditAvatarClick(int position) {
        final SettingsUsersUserItemModel userItem = getItemAtPosition(position);
        avatarIdentityId = userItem.getId();
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            final SettingsUsersUserItemModel itemModel = getItemAtPosition(i);
            if (Objects.equals(itemModel.getId(), avatarIdentityId)) {
                userRepo.updatePendingIdentityAvatar(avatarIdentityId, avatarPath);
                break;
            }
        }
    }

    @Override
    public void onRemoveClick(final int position) {
        final SettingsUsersUserItemModel itemModel = getItemAtPosition(position);
        if (!Objects.equals(itemModel.getBalance(), BigFraction.ZERO)) {
            view.showMessage(R.string.toast_del_identity_balance_not_zero);
            return;
        }

        getSubscriptions().add(groupRepo.removePendingIdentity(itemModel.getId(), itemModel.getGroupId())
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity value) {
                        view.showMessage(R.string.toast_settings_users_removed);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to remove pending identity with error:");
                        view.showMessage(R.string.toast_error_settings_users_remove);
                    }
                })
        );
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        nickname = s.toString();
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onAddUserClick(View view) {
        if (validate()) {
            groupRepo.addPendingIdentity(currentIdentity, nickname);
            setValidate(false);
            setNickname("");
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }
}
