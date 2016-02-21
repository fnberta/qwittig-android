/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.addusers.items.HeaderItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.IntroItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.AddUsersItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.UserItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 08.02.16.
 */
public class SettingsAddUsersViewModelImpl extends ListViewModelBaseImpl<AddUsersItem, SettingsAddUsersViewModel.ViewListener>
        implements SettingsAddUsersViewModel {

    private static final String STATE_ITEMS = "STATE_ITEMS";

    public SettingsAddUsersViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull SettingsAddUsersViewModel.ViewListener view,
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
            return;
        }

        mItems.add(new IntroItem());
        mItems.add(new NicknameItem());
        mItems.add(new HeaderItem(R.string.header_users_added));

        final Group group = mCurrentIdentity.getGroup();
        getSubscriptions().add(mIdentityRepo.getIdentitiesLocalAsync(group, true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return identity.isPending();
                    }
                })
                .subscribe(new Subscriber<Identity>() {
                    final String groupName = group.getName();

                    @Override
                    public void onCompleted() {
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO: handle error
                    }

                    @Override
                    public void onNext(Identity identity) {
                        mItems.add(new UserItem(identity.getNickname(),
                                mIdentityRepo.getInvitationUrl(identity, groupName)));
                    }
                })
        );
    }

    @Override
    public void onAddUserClick(@NonNull NicknameItem nicknameItem) {
        if (nicknameItem.validate()) {
            final Group group = mCurrentIdentity.getGroup();
            mView.toggleProgressDialog(true);
            mView.loadAddUserWorker(nicknameItem.getNickname(), group.getObjectId(), group.getName());
            nicknameItem.setValidate(false);
        }
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String invitationUrl) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        // TODO: hardcode position?
                        final NicknameItem nicknameItem = ((NicknameItem) mItems.get(1));
                        mItems.add(new UserItem(nicknameItem.getNickname(), invitationUrl));
                        mView.notifyItemInserted(getLastPosition());
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
