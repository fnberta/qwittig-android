/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;

import java.io.File;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link HomeViewModel}.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private static final String STATE_INVITATION_ID = "STATE_INVITATION_ID";
    private static final String STATE_OCR_PURCHASE_ID = "OCR_PURCHASE_ID";
    private static final String STATE_OCR_PROCESSING = "STATE_OCR_PROCESSING";
    private final PurchaseRepository mPurchaseRepo;
    private boolean mOcrProcessing;
    private boolean mAnimStop;
    private boolean mDraftsAvailable;
    private String mInvitationIdentityId;
    private String mOcrPurchaseId;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull HomeViewModel.ViewListener view,
                             @NonNull UserRepository userRepository,
                             @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, userRepository);

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
    public void onLoginSuccessful() {
        mCurrentUser = mUserRepo.getCurrentUser();
        setCurrentIdentity();
        mDraftsAvailable = mPurchaseRepo.isDraftsAvailable(mCurrentIdentity);
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

                mView.showMessage(R.string.toast_error_unknown);
                // TODO: show proper error message, define error codes in cloud code
            }
        }));
    }

    @Override
    public void onReceiptImageTaken(@NonNull String receiptImagePath) {
        startProgress();
//        mView.showMessage(R.string.toast_purchase_ocr_started);
        mView.loadOcrWorker(receiptImagePath);
    }

    @Override
    public void onReceiptImageFailed() {
        mView.showMessage(R.string.toast_purchase_discarded);
    }

    @Override
    public void setOcrStream(@NonNull Single<String> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String receiptPath) {
                        mView.removeWorker(workerTag);
                        deleteReceiptImage(receiptPath);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);

                        mView.showMessage(mPurchaseRepo.getErrorMessage(error));
                        stopProgress(false);
                    }
                })
        );
    }

    private void deleteReceiptImage(@NonNull String receiptPath) {
        if (!TextUtils.isEmpty(receiptPath)) {
            final File receipt = new File(receiptPath);
            final boolean fileDeleted = receipt.delete();
            if (!fileDeleted && BuildConfig.DEBUG) {
                Timber.e("failed to delete receipt image file");
            }
        }
    }

    @Override
    public void onOcrPurchaseReady(@NonNull String ocrPurchaseId) {
        mOcrPurchaseId = ocrPurchaseId;
        stopProgress(true);
    }

    @Override
    public void onOcrPurchaseFailed() {
        stopProgress(false);
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
                        mView.startPurchaseAddScreen(null);
                        break;
                }
            }

            @Override
            public void onFabCompleteClicked() {
                mView.startPurchaseAddScreen(mOcrPurchaseId);
            }
        };
    }

    private void startProgress() {
        mOcrProcessing = true;
        notifyPropertyChanged(BR.ocrProcessing);
    }

    private void stopProgress(boolean animate) {
        mOcrProcessing = false;
        mAnimStop = animate;
        notifyPropertyChanged(BR.ocrProcessing);
    }
}
