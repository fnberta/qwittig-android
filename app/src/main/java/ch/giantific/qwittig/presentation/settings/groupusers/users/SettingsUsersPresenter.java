/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber2;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsUsersContract}.
 */
public class SettingsUsersPresenter extends BasePresenterImpl<SettingsUsersContract.ViewListener>
        implements SettingsUsersContract.Presenter {

    private final SettingsUsersViewModel viewModel;
    private final GroupRepository groupRepo;
    private Identity currentIdentity;

    @Inject
    public SettingsUsersPresenter(@NonNull Navigator navigator,
                                  @NonNull SettingsUsersViewModel viewModel,
                                  @NonNull UserRepository userRepo,
                                  @NonNull GroupRepository groupRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
    }

    @Override
    public int compareItemViewModels(@NonNull SettingsUsersItemViewModel item1, @NonNull SettingsUsersItemViewModel item2) {
        return item1.compareTo(item2);
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
                        view.clearItems();

                        final String groupId = identity.getGroup();
                        addDataListener(groupId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String groupId) {
        final Observable<List<SettingsUsersItemViewModel>> initialData = groupRepo.getGroupIdentities(groupId, true)
                .flatMap((identity) -> getItemViewModel(EventType.NONE, identity))
                .toList()
                .doOnNext(settingsUsersItemViewModels -> {
                    view.addItems(settingsUsersItemViewModels);
                    viewModel.setEmpty(view.isItemsEmpty());
                    view.startEnterTransition();
                });
        subscriptions.add(groupRepo.observeGroupIdentityChildren(groupId)
                .skipUntil(initialData)
                .flatMap(event -> getItemViewModel(event.getEventType(), event.getValue()))
                .subscribe(new ChildEventSubscriber2<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_users_load)))
        );
    }

    private Observable<SettingsUsersItemViewModel> getItemViewModel(@EventType int eventType,
                                                                    @NonNull Identity identity) {
        return groupRepo.getGroup(identity.getGroup()).toObservable()
                .map(group -> new SettingsUsersItemViewModel(eventType, identity, group.getName()));
    }

    @Override
    public void onInviteClick(int position) {
        final SettingsUsersItemViewModel userItem = view.getItemAtPosition(position);
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
        final SettingsUsersItemViewModel userItem = view.getItemAtPosition(position);
        view.showChangeNicknameDialog(userItem.getNickname(), position);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        final SettingsUsersItemViewModel itemViewModel = view.getItemAtPosition(position);
        userRepo.updatePendingIdentityNickname(itemViewModel.getId(), nickname);
    }

    @Override
    public void onEditAvatarClick(int position) {
        final SettingsUsersItemViewModel userItem = view.getItemAtPosition(position);
        viewModel.setAvatarIdentityId(userItem.getId());
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        userRepo.updatePendingIdentityAvatar(viewModel.getAvatarIdentityId(), avatarPath);
    }

    @Override
    public void onRemoveClick(final int position) {
        final SettingsUsersItemViewModel itemViewModel = view.getItemAtPosition(position);
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
                        Timber.e(error, "failed to removeItem pending identity with error:");
                        view.showMessage(R.string.toast_error_settings_users_remove);
                    }
                })
        );
    }

    @Override
    public void onAddUserClick(View view) {
        if (viewModel.isInputValid()) {
            groupRepo.addPendingIdentity(currentIdentity, viewModel.nickname.get());
            viewModel.setValidate(false);
            viewModel.nickname.set("");
        }
    }
}
