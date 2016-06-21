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

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.bus.events.EventDraftDeleted;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;

/**
 * Provides an implementation of the {@link HomeViewModel}.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private static final String STATE_INVITATION_ID = "STATE_INVITATION_ID";
    private static final String STATE_OCR_PURCHASE_ID = "OCR_PURCHASE_ID";
    private static final String STATE_OCR_PROCESSING = "STATE_OCR_PROCESSING";
    private final Navigator mNavigator;
    private final PurchaseRepository mPurchaseRepo;
    private boolean mOcrProcessing;
    private boolean mAnimStop;
    private boolean mDraftsAvailable;
    private String mInvitationIdentityId;
    private String mOcrPurchaseId;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull Navigator navigator,
                             @NonNull RxBus<Object> eventBus,
                             @NonNull UserRepository userRepository,
                             @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, eventBus, userRepository);
        mNavigator = navigator;

        mPurchaseRepo = purchaseRepo;
        if (savedState != null) {
            mInvitationIdentityId = savedState.getString(STATE_INVITATION_ID, "");
            mOcrPurchaseId = savedState.getString(STATE_OCR_PURCHASE_ID, "");
            mOcrProcessing = savedState.getBoolean(STATE_OCR_PROCESSING);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(mInvitationIdentityId)) {
            outState.putString(STATE_INVITATION_ID, mInvitationIdentityId);
        }
        if (!TextUtils.isEmpty(mOcrPurchaseId)) {
            outState.putString(STATE_OCR_PURCHASE_ID, mOcrPurchaseId);
        }
        outState.putBoolean(STATE_OCR_PROCESSING, mOcrProcessing);
    }

    @Override
    public void onViewVisible() {
        super.onViewVisible();

        getSubscriptions().add(mEventBus.observeEvents(EventDraftDeleted.class)
                .subscribe(new Action1<EventDraftDeleted>() {
                    @Override
                    public void call(EventDraftDeleted eventDraftDeleted) {
                        checkDrafts();
                    }
                })
        );
    }

    @Override
    public void onLoginSuccessful() {
        mCurrentUser = mUserRepo.getCurrentUser();
        setCurrentIdentity();
        mDraftsAvailable = mPurchaseRepo.isDraftsAvailable(mCurrentIdentity);
    }

    @Override
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        super.onIdentitySelected(identitySelected);

        checkDrafts();
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
    public void checkDrafts() {
        if (mDraftsAvailable != updateDraftsAvailable()) {
            mView.toggleDraftTab(mDraftsAvailable);
        }
    }

    @Override
    public boolean updateDraftsAvailable() {
        final boolean draftsAvailable = mPurchaseRepo.isDraftsAvailable(mCurrentIdentity);
        setDraftsAvailable(draftsAvailable);

        return draftsAvailable;
    }

    @Override
    public void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                                 @NonNull String inviterNickname) {
        mInvitationIdentityId = identityId;
        mView.showGroupJoinDialog(groupName, inviterNickname);
    }

    @Override
    public void onJoinInvitedGroupSelected() {
        mView.showProgressDialog(R.string.progress_joining_group);
        mView.loadJoinGroupWorker(mInvitationIdentityId);
    }

    @Override
    public void onDiscardInvitationSelected() {
        // do nothing
    }

    @Override
    public void setJoinGroupStream(@NonNull Single<Identity> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Identity>() {
            @Override
            public void onSuccess(Identity identity) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_group_joined);
                mView.onGroupJoined();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_error_join_group);
            }
        }));
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

                        mView.showMessage(R.string.toast_error_purchase_ocr_process);
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
        mView.showMessage(R.string.toast_error_purchase_ocr_process);
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

//    @Override
//    public void onViewGone() {
//        super.onViewGone();
//
//        stopProgress(false);
//    }
}
