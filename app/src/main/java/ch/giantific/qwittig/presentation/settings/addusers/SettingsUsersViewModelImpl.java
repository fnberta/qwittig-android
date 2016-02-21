/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

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
import ch.giantific.qwittig.presentation.settings.addusers.items.HeaderItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.IntroItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.SettingsUsersItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.UserItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 08.02.16.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersItem, SettingsUsersViewModel.ViewListener>
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

        mItems.add(new IntroItem());
        mItems.add(new NicknameItem());
        mItems.add(new HeaderItem(R.string.header_settings_users));

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
                .map(new Func1<Identity, UserItem>() {
                    @Override
                    public UserItem call(Identity identity) {
                        return identity.isPending()
                                ? new UserItem(identity.getNickname(), mIdentityRepo.getInvitationUrl(identity, groupName))
                                : new UserItem(identity.getNickname());
                    }
                })
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<UserItem>>() {
                    @Override
                    public void onSuccess(List<UserItem> items) {
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

                        // TODO: hardcode item position?
                        final NicknameItem nicknameItem = ((NicknameItem) mItems.get(POS_NICKNAME));
                        nicknameItem.setValidate(false);
                        nicknameItem.setNickname("");
                        mItems.add(new UserItem(nicknameItem.getNickname(), invitationUrl));
                        mView.notifyItemInserted(getLastPosition());
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
