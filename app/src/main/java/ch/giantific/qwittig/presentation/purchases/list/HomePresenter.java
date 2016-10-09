/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link HomeContract}.
 */
public class HomePresenter extends BasePresenterImpl<HomeContract.ViewListener>
        implements HomeContract.Presenter {

    private final HomeViewModel viewModel;
    private final GroupRepository groupRepo;
    private final PurchaseRepository purchaseRepo;
    private Identity currentIdentity;
    private String currentUserId;

    @Inject
    public HomePresenter(@NonNull Navigator navigator,
                         @NonNull HomeViewModel viewModel,
                         @NonNull UserRepository userRepo,
                         @NonNull GroupRepository groupRepo,
                         @NonNull PurchaseRepository purchaseRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
        this.purchaseRepo = purchaseRepo;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        currentUserId = currentUser.getUid();
        subscriptions.add(userRepo.observeCurrentIdentityId(currentUserId)
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .doOnNext(identity -> {
                    currentIdentity = identity;
                    // listen to group identities, otherwise we wouldn't catch newly added users
                    observeGroupIdentities(identity.getGroup());
                })
                .flatMap(identity -> purchaseRepo.isDraftsAvailable(identity.getGroup(), identity.getId()))
                .doOnNext(viewModel::setDraftsAvailable)
                .subscribe(new IndefiniteSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean draftsAvailable) {
                        view.toggleDraftTab(draftsAvailable);
                    }
                })
        );

        subscriptions.add(purchaseRepo.observeOcrData(currentUserId, false)
                .subscribe(new IndefiniteSubscriber<List<OcrData>>() {
                    @Override
                    public void onNext(List<OcrData> ocrData) {
                        viewModel.setOcrPurchaseId(!ocrData.isEmpty() ? ocrData.get(0).getId() : "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.push_purchase_ocr_failed_alert);
                    }
                })
        );
    }

    private void observeGroupIdentities(@NonNull String groupId) {
        subscriptions.add(groupRepo.observeGroupIdentityChildren(groupId)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(),
                        currentIdentity.getGroup()))
                .subscribe()
        );
    }

    @Override
    public void handleInvitation(@NonNull String identityId,
                                 @NonNull String groupName,
                                 @NonNull String inviterNickname) {
        view.showGroupJoinDialog(identityId, groupName, inviterNickname);
    }

    @Override
    public void onJoinInvitedGroupSelected(@NonNull final String identityId) {
        subscriptions.add(userRepo.getIdentity(identityId)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        groupRepo.joinGroup(identity, currentUserId, currentIdentity.getNickname(),
                                currentIdentity.getAvatar());
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "Failed to join invited group with error:");
                        view.showMessage(R.string.toast_error_join_group);
                    }
                })
        );
    }

    @Override
    public void onDiscardInvitationSelected() {
        // do nothing
    }

    @Override
    public void onReceiptImageTaken(@NonNull String receipt) {
        view.showMessage(R.string.toast_purchase_ocr_started);
        purchaseRepo.uploadReceiptForOcr(receipt, currentUserId);
    }

    @Override
    public void onReceiptImageDiscarded() {
        view.showMessage(R.string.toast_purchase_discarded);
    }

    @Override
    public void onReceiptImageFailed() {
        view.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public FabMenuClickListener getFabMenuClickListener() {
        return new FabMenuClickListener() {
            @Override
            public void onFabMenuItemClicked(@NonNull MenuItem menuItem) {
                final int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_fab_home_auto:
                        view.captureImage();
                        break;
                    case R.id.action_fab_home_manual:
                        navigator.startPurchaseAdd(null);
                        break;
                }
            }

            @Override
            public void onFabCompleteClicked() {
                final String ocrPurchaseId = viewModel.getOcrPurchaseId();
                view.clearOcrNotification(ocrPurchaseId);
                navigator.startPurchaseAdd(ocrPurchaseId);
            }
        };
    }
}
