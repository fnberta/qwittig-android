/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link HomeViewModel}.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private static final String STATE_OCR_PURCHASE_ID = "OCR_PURCHASE_ID";
    private static final String STATE_OCR_PROCESSING = "STATE_OCR_PROCESSING";
    private static final String STATE_DRAFTS_AVAILABLE = "STATE_DRAFTS_AVAILABLE";
    private final GroupRepository mGroupRepo;
    private final PurchaseRepository mPurchaseRepo;
    private String mCurrentUserId;
    private boolean mOcrProcessing;
    private boolean mAnimStop;
    private boolean mDraftsAvailable;
    private String mOcrPurchaseId;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull Navigator navigator,
                             @NonNull RxBus<Object> eventBus,
                             @NonNull UserRepository userRepository,
                             @NonNull GroupRepository groupRepository,
                             @NonNull PurchaseRepository purchaseRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mGroupRepo = groupRepository;
        mPurchaseRepo = purchaseRepository;

        if (savedState != null) {
            mOcrPurchaseId = savedState.getString(STATE_OCR_PURCHASE_ID, "");
            mOcrProcessing = savedState.getBoolean(STATE_OCR_PROCESSING);
            mDraftsAvailable = savedState.getBoolean(STATE_DRAFTS_AVAILABLE);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(mOcrPurchaseId)) {
            outState.putString(STATE_OCR_PURCHASE_ID, mOcrPurchaseId);
        }
        outState.putBoolean(STATE_OCR_PROCESSING, mOcrProcessing);
        outState.putBoolean(STATE_DRAFTS_AVAILABLE, mDraftsAvailable);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        mCurrentUserId = currentUser.getUid();
        getSubscriptions().add(mUserRepo.observeUser(mCurrentUserId)
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .doOnNext(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        // listen to group identities, otherwise we wouldn't catch newly added users
                        observeGroupIdentities(identity.getGroup());
                    }
                })
                .flatMap(new Func1<Identity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Identity identity) {
                        return mPurchaseRepo.isDraftsAvailable(identity.getGroup(), identity.getId());
                    }
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean draftsAvailable) {
                        setDraftsAvailable(draftsAvailable);
                    }
                })
                .subscribe(new IndefiniteSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean draftsAvailable) {
                        mView.toggleDraftTab(draftsAvailable);
                    }
                })
        );
    }

    private void observeGroupIdentities(@NonNull String groupId) {
        getSubscriptions().add(mUserRepo.observeGroupIdentityChildren(groupId).subscribe());
    }

    @Override
    @Bindable
    public boolean isOcrProcessing() {
        return mOcrProcessing;
    }

    @Override
    @Bindable
    public boolean isAnimStop() {
        return mAnimStop;
    }

    @Override
    public void startProgress() {
        mOcrProcessing = true;
        notifyPropertyChanged(BR.ocrProcessing);
    }

    @Override
    public void stopProgress(boolean animate) {
        mOcrProcessing = false;
        mAnimStop = animate;
        notifyPropertyChanged(BR.ocrProcessing);
    }

    @Override
    @Bindable
    public boolean isDraftsAvailable() {
        return mDraftsAvailable;
    }

    @Override
    public void setDraftsAvailable(boolean available) {
        mDraftsAvailable = available;
        notifyPropertyChanged(BR.draftsAvailable);
    }

    @Override
    public void handleInvitation(@NonNull String identityId,
                                 @NonNull String groupName,
                                 @NonNull String inviterNickname) {
        mView.showGroupJoinDialog(identityId, groupName, inviterNickname);
    }

    @Override
    public void onJoinInvitedGroupSelected(@NonNull final String identityId) {
        getSubscriptions().add(mUserRepo.getIdentity(identityId)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        mGroupRepo.joinGroup(mCurrentUserId, identity.getGroup(), identityId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_join_group);
                        Timber.e(error, "Failed to join invited group with error:");
                    }
                })
        );
    }

    @Override
    public void onDiscardInvitationSelected() {
        // do nothing
    }

    @Override
    public void onReceiptImageTaken(@NonNull byte[] receipt) {
//        startProgress();
        mView.showMessage(R.string.toast_purchase_ocr_started);
        mView.loadOcrWorker(receipt);
    }

    @Override
    public void onReceiptImageDiscarded() {
        mView.showMessage(R.string.toast_purchase_discarded);
    }

    @Override
    public void onReceiptImageFailed() {
        mView.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public void setOcrStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mView.removeWorker(workerTag);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);

                        mView.showMessage(R.string.push_purchase_ocr_failed_alert);
                        stopProgress(false);
                    }
                })
        );
    }

    @Override
    public void onOcrPurchaseReady(@NonNull String ocrPurchaseId) {
        mOcrPurchaseId = ocrPurchaseId;
//        stopProgress(true);
    }

    @Override
    public void onOcrPurchaseFailed() {
//        stopProgress(false);
        mView.showMessage(R.string.push_purchase_ocr_failed_alert);
    }

    @Override
    public FabMenuClickListener getFabMenuClickListener() {
        return new FabMenuClickListener() {
            @Override
            public void onFabMenuItemClicked(@NonNull MenuItem menuItem) {
                final int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_fab_home_auto:
                        mView.captureImage();
                        break;
                    case R.id.action_fab_home_manual:
                        mNavigator.startPurchaseAdd(null);
                        break;
                }
            }

            @Override
            public void onFabCompleteClicked() {
                mNavigator.startPurchaseAdd(mOcrPurchaseId);
            }
        };
    }
}
