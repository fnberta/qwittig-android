/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersUserItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersHeaderItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersIntroItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersNicknameItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersBaseItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsUsersViewModel}.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersBaseItem, SettingsUsersViewModel.ViewListener>
        implements SettingsUsersViewModel {

    private static final String STATE_ITEMS = "STATE_ITEMS";
    private static final int POS_NICKNAME = 1;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull SettingsUsersViewModel.ViewListener view,
                                      @NonNull IdentityRepository identityRepo,
                                      @NonNull UserRepository userRepository) {
        super(savedState, view, identityRepo, userRepository);

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ITEMS);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ITEMS, mItems);
    }

    @Override
    public void loadData() {
        if (!mItems.isEmpty()) {
            // list is already filled, return immediately
            return;
        }

        mItems.add(new SettingsUsersIntroItem());
        mItems.add(new SettingsUsersNicknameItem());
        mItems.add(new SettingsUsersHeaderItem(R.string.header_settings_users));

        final Group group = mCurrentIdentity.getGroup();
        final String groupName = group.getName();
        final String currentId = mCurrentIdentity.getObjectId();
        getSubscriptions().add(mIdentityRepo.getIdentitiesLocalAsync(group, true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !identity.getObjectId().equals(currentId);
                    }
                })
                .map(new Func1<Identity, SettingsUsersUserItem>() {
                    @Override
                    public SettingsUsersUserItem call(Identity identity) {
                        return identity.isPending()
                                ? new SettingsUsersUserItem(identity.getNickname(), mIdentityRepo.getInvitationUrl(identity, groupName))
                                : new SettingsUsersUserItem(identity.getNickname());
                    }
                })
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<SettingsUsersUserItem>>() {
                    @Override
                    public void onSuccess(List<SettingsUsersUserItem> items) {
                        mItems.addAll(items);
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
    public void onValidUserEntered(@NonNull String nickname) {
        final Group group = mCurrentIdentity.getGroup();
        mView.toggleProgressDialog(true);
        mView.loadAddUserWorker(nickname, group.getObjectId(), group.getName());
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String invitationUrl) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        final SettingsUsersNicknameItem nicknameItem =
                                ((SettingsUsersNicknameItem) mItems.get(POS_NICKNAME));
                        mItems.add(new SettingsUsersUserItem(nicknameItem.getNickname(), invitationUrl));
                        mView.notifyItemInserted(getLastPosition());
                        nicknameItem.setValidate(false);
                        nicknameItem.setNickname("");
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

    @Override
    public void onShareClick(@NonNull String shareLink) {
        mView.loadLinkShareOptions(shareLink);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }
}
