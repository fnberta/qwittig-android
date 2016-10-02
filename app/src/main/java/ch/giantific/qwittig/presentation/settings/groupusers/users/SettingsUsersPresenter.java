/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsUsersContract}.
 */
public class SettingsUsersPresenter extends BasePresenterImpl<SettingsUsersContract.ViewListener>
        implements SettingsUsersContract.Presenter {

    private static final String STATE_VIEW_MODEL = SettingsUsersViewModel.class.getCanonicalName();
    private static final String STATE_AVATAR_IDENTITY_ID = "STATE_AVATAR_IDENTITY_ID";
    private final SettingsUsersViewModel viewModel;
    private final SortedList<SettingsUsersItemViewModel> items;
    private final SortedListCallback<SettingsUsersItemViewModel> listCallback;
    private final GroupRepository groupRepo;
    private boolean initialDataLoaded;
    private Identity currentIdentity;
    private String avatarIdentityId;

    public SettingsUsersPresenter(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull UserRepository userRepo,
                                  @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, userRepo);

        this.groupRepo = groupRepo;

        listCallback = new SortedListCallback<SettingsUsersItemViewModel>() {
            @Override
            public int compare(SettingsUsersItemViewModel o1, SettingsUsersItemViewModel o2) {
                return o1.compareTo(o2);
            }
        };
        items = new SortedList<>(SettingsUsersItemViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
            avatarIdentityId = savedState.getString(STATE_AVATAR_IDENTITY_ID);
        } else {
            viewModel = new SettingsUsersViewModel();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
        if (!TextUtils.isEmpty(avatarIdentityId)) {
            outState.putString(STATE_AVATAR_IDENTITY_ID, avatarIdentityId);
        }
    }

    @Override
    public SettingsUsersViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        currentIdentity = identity;
                        viewModel.setGroupName(identity.getGroupName());
                        items.clear();

                        initialDataLoaded = false;
                        final String groupId = identity.getGroup();
                        addDataListener(groupId);
                        loadInitialData(groupId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String groupId) {
        subscriptions.add(groupRepo.observeGroupIdentityChildren(groupId)
                .filter(childEvent -> initialDataLoaded)
                .flatMap(event -> getItemViewModel(event.getEventType(), event.getValue()))
                .subscribe(new ChildEventSubscriber<>(items, viewModel, e ->
                        view.showMessage(R.string.toast_error_users_load)))
        );
    }

    private void loadInitialData(@NonNull String groupId) {
        subscriptions.add(groupRepo.getGroupIdentities(groupId, true)
                .flatMap((identity) -> getItemViewModel(EventType.NONE, identity))
                .toList()
                .subscribe(new Subscriber<List<SettingsUsersItemViewModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        viewModel.setEmpty(getItemCount() == 0);
                        view.startEnterTransition();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "failed to load initial users with error:");
                        view.showMessage(R.string.toast_error_users_load);
                    }

                    @Override
                    public void onNext(List<SettingsUsersItemViewModel> purchaseItemModels) {
                        items.addAll(purchaseItemModels);
                    }
                })
        );
    }

    private Observable<SettingsUsersItemViewModel> getItemViewModel(@EventType int eventType,
                                                                    @NonNull Identity identity) {
        return groupRepo.getGroup(identity.getGroup()).toObservable()
                .map(group -> new SettingsUsersItemViewModel(eventType, identity, group.getName()));
    }

    @Override
    public SettingsUsersItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onInviteClick(int position) {
        final SettingsUsersItemViewModel userItem = getItemAtPosition(position);
        final String invitationLink = groupRepo.getInvitationLink(userItem.getId(),
                userItem.getGroupName(), currentIdentity.getNickname());
        final String googleApiKey = view.getGoogleApiKey();
        subscriptions.add(groupRepo.shortenUrl(invitationLink, googleApiKey)
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String shortUrl) {
                        view.loadLinkShareOptions(shortUrl);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to get short URL with error:");
                    }
                })
        );

        // TODO: protect against configuration changes
    }

    @Override
    public void onEditNicknameClick(int position) {
        final SettingsUsersItemViewModel userItem = getItemAtPosition(position);
        view.showChangeNicknameDialog(userItem.getNickname(), position);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        final SettingsUsersItemViewModel itemViewModel = getItemAtPosition(position);
        userRepo.updatePendingIdentityNickname(itemViewModel.getId(), nickname);
    }

    @Override
    public void onEditAvatarClick(int position) {
        final SettingsUsersItemViewModel userItem = getItemAtPosition(position);
        avatarIdentityId = userItem.getId();
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            final SettingsUsersItemViewModel itemViewModel = getItemAtPosition(i);
            if (Objects.equals(itemViewModel.getId(), avatarIdentityId)) {
                userRepo.updatePendingIdentityAvatar(avatarIdentityId, avatarPath);
                break;
            }
        }
    }

    @Override
    public void onRemoveClick(final int position) {
        final SettingsUsersItemViewModel itemViewModel = getItemAtPosition(position);
        if (!Objects.equals(itemViewModel.getBalance(), BigFraction.ZERO)) {
            view.showMessage(R.string.toast_del_identity_balance_not_zero);
            return;
        }

        subscriptions.add(groupRepo.removePendingIdentity(itemViewModel.getId(), itemViewModel.getGroupId())
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
        viewModel.setNickname(s.toString());
    }

    @Override
    public void onAddUserClick(View view) {
        if (viewModel.isInputValid()) {
            groupRepo.addPendingIdentity(currentIdentity, viewModel.getNickname());
            viewModel.setValidate(false);
            viewModel.setNickname("");
        }
    }
}
