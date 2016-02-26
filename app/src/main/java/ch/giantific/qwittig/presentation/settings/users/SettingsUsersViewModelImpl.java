/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersBaseItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersHeaderItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersIntroItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersNicknameItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersUserItem;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsUsersViewModel}.
 */
public class SettingsUsersViewModelImpl extends ListViewModelBaseImpl<SettingsUsersBaseItem, SettingsUsersViewModel.ViewListener>
        implements SettingsUsersViewModel {

    private static final int POS_NICKNAME = 1;

    public SettingsUsersViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull SettingsUsersViewModel.ViewListener view,
                                      @NonNull IdentityRepository identityRepo,
                                      @NonNull UserRepository userRepository) {
        super(savedState, view, identityRepo, userRepository);

        if (savedState != null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    public void loadData() {
        final SettingsUsersViewModel listener = this;

        mItems.add(new SettingsUsersIntroItem());
        mItems.add(new SettingsUsersNicknameItem(listener));
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
                                ? new SettingsUsersUserItem(listener, identity,
                                mIdentityRepo.getInvitationUrl(identity, groupName))
                                : new SettingsUsersUserItem(listener, identity);
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
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull final String workerTag) {
        final SettingsUsersViewModel listener = this;
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        mView.removeWorker(workerTag);
                        mView.toggleProgressDialog(false);

                        final Group group = mCurrentIdentity.getGroup();
                        final String groupName = group.getName();
                        mItems.add(new SettingsUsersUserItem(listener, identity,
                                mIdentityRepo.getInvitationUrl(identity, groupName)));
                        mView.notifyItemInserted(getLastPosition());

                        final SettingsUsersNicknameItem nicknameItem =
                                ((SettingsUsersNicknameItem) mItems.get(POS_NICKNAME));
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

    @Override
    public boolean isItemDismissable(int position) {
        if (getItemViewType(position) != SettingsUsersBaseItem.Type.USER) {
            return false;
        }

        final SettingsUsersUserItem userItem = (SettingsUsersUserItem) mItems.get(position);
        return userItem.isPending() && userItem.getIdentity().getBalance().equals(BigFraction.ZERO);
    }

    @Override
    public void onItemDismiss(final int position) {
        final SettingsUsersUserItem userItem = (SettingsUsersUserItem) mItems.get(position);
        final Identity identity = userItem.getIdentity();
        getSubscriptions().add(mIdentityRepo.removePendingIdentity(identity)
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
}
