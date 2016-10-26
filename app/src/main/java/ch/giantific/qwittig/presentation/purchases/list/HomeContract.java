/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.support.annotation.NonNull;

import ch.berta.fabio.fabspeeddial.FabMenuClickListener;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for the home screen.
 */
public interface HomeContract {

    interface Presenter extends BasePresenter<ViewListener>,
            JoinGroupDialogFragment.DialogInteractionListener {

        void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                              @NonNull String inviterNickname);

        void onReceiptImageTaken(@NonNull String receipt);

        void onReceiptImageDiscarded();

        void onReceiptImageFailed();

        FabMenuClickListener getFabMenuClickListener();
    }

    interface ViewListener extends BaseView {
        void showGroupJoinDialog(@NonNull String identityId,
                                 @NonNull String groupName,
                                 @NonNull String inviterNickname);

        void captureImage();

        void toggleDraftTab(boolean draftsAvailable);

        void clearOcrNotification(@NonNull String ocrPurchaseId);
    }
}
