/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.IntroItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.ListItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.UserItem;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 08.02.16.
 */
public class SettingsAddUsersViewModelImpl extends ListViewModelBaseImpl<ListItem, SettingsAddUsersViewModel.ViewListener>
        implements SettingsAddUsersViewModel {

    public SettingsAddUsersViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull SettingsAddUsersViewModel.ViewListener view,
                                         @NonNull IdentityRepository identityRepo,
                                         @NonNull UserRepository userRepository) {
        super(savedState, view, identityRepo, userRepository);
    }

    @Override
    public void loadData() {
        if (mItems.isEmpty()) {
            mItems.add(new IntroItem());
            mItems.add(new NicknameItem());
            mView.notifyDataSetChanged();
        }
    }

    @Override
    public void onAddUserClick(@NonNull NicknameItem nicknameItem) {
        if (nicknameItem.validate()) {
            final Group group = mCurrentIdentity.getGroup();
            mView.toggleProgressDialog(true);
            mView.loadAddUserWorker(nicknameItem.getNickname(), group.getObjectId(), group.getName());
        }
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String invitationUrl) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        final NicknameItem nicknameItem = ((NicknameItem) mItems.get(1));
                        final int lastPosition = getLastPosition();
                        mItems.add(lastPosition, new UserItem(nicknameItem.getNickname(), invitationUrl));
                        mView.notifyItemInserted(lastPosition);
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

    @Override
    public void onShareClick(@NonNull String shareLink) {
        mView.loadLinkShareOptions(shareLink);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }
}
