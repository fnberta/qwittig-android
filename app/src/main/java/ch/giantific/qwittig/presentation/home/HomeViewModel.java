/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 22.01.16.
 */
public interface HomeViewModel extends ViewModel,
        JoinGroupDialogFragment.DialogInteractionListener,
        JoinGroupWorkerListener {

    @Bindable
    boolean isDraftsAvailable();

    void setDraftsAvailable(boolean available);

    boolean updateDraftsAvailable();

    void handleInvitation(@NonNull String identityId, @NonNull String groupName);

    void onFabAddPurchaseManualClick(View view);

    void onFabAddPurchaseAutoClick(View view);

    interface ViewListener extends ViewModel.ViewListener {
        void startPurchaseAddActivity(boolean autoMode);

        void showGroupJoinDialog(@NonNull String groupName);

        void loadJoinGroupWorker(@NonNull String identityId);

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void onGroupJoined();
    }
}
