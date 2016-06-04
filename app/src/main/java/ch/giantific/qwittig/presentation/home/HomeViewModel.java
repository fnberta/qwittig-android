/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the home screen.
 */
public interface HomeViewModel extends ViewModel,
        JoinGroupDialogFragment.DialogInteractionListener,
        JoinGroupWorkerListener, OcrWorkerListener {

    void onLoginSuccessful();

    @Bindable
    boolean isDraftsAvailable();

    void setDraftsAvailable(boolean available);

    boolean updateDraftsAvailable();

    void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                          @NonNull String inviterNickname);

    void onReceiptImageTaken(@NonNull String receiptImagePath);

    void onReceiptImageFailed();

    void onOcrPurchaseReady(@NonNull String ocrPurchaseId);

    void onOcrPurchaseFailed();

    FabMenu.FabMenuItemClickListener getFabMenuItemClickListener();

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

        void loadOcrWorker(@NonNull String receiptImagePath);

        void startPurchaseAddScreen(@Nullable String ocrPurchaseId);
    }
}
