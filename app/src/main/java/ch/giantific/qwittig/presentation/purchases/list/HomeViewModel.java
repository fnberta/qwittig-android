/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the home screen.
 */
public interface HomeViewModel extends ViewModel<HomeViewModel.ViewListener>,
        JoinGroupDialogFragment.DialogInteractionListener {

    @Bindable
    boolean isOcrAvailable();

    @Bindable
    boolean isDraftsAvailable();

    void setDraftsAvailable(boolean available);

    void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                          @NonNull String inviterNickname);

    void onReceiptImageTaken(@NonNull String receipt);

    void onReceiptImageDiscarded();

    void onReceiptImageFailed();

    FabMenuClickListener getFabMenuClickListener();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void showGroupJoinDialog(@NonNull String identityId,
                                 @NonNull String groupName,
                                 @NonNull String inviterNickname);

        void captureImage();

        void toggleDraftTab(boolean draftsAvailable);

        void clearOcrNotification(@NonNull String ocrPurchaseId);
    }
}
