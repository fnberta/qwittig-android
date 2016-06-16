/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.berta.fabio.fabspeeddial.ProgressFinalAnimationListener;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the home screen.
 */
public interface HomeViewModel extends ViewModel,
        JoinGroupDialogFragment.DialogInteractionListener,
        JoinGroupWorkerListener, OcrWorkerListener {

    void onLoginSuccessful();

    @Bindable
    boolean isOcrProcessing();

    @Bindable
    boolean isAnimStop();

    void startProgress();

    void stopProgress(boolean animate);

    @Bindable
    boolean isDraftsAvailable();

    void setDraftsAvailable(boolean available);

    void checkDrafts();

    boolean updateDraftsAvailable();

    void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                          @NonNull String inviterNickname);

    void onReceiptImageTaken(@NonNull byte[] receipt);

    void onReceiptImageDiscarded();

    void onReceiptImageFailed();

    void onOcrPurchaseReady(@NonNull String ocrPurchaseId);

    void onOcrPurchaseFailed();

    FabMenuClickListener getFabMenuClickListener();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void showGroupJoinDialog(@NonNull String groupName, @NonNull String inviterNickname);

        void loadJoinGroupWorker(@NonNull String identityId);

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void onGroupJoined();

        void captureImage();

        void loadOcrWorker(@NonNull byte[] receipt);

        void startPurchaseAddScreen(@Nullable String ocrPurchaseId);

        void toggleDraftTab(boolean draftsAvailable);
    }
}
