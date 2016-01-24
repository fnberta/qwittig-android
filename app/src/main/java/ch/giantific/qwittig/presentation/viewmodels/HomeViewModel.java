/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.view.View;

import ch.giantific.qwittig.presentation.ui.fragments.dialogs.GroupJoinDialogFragment;
import ch.giantific.qwittig.presentation.workerfragments.group.InvitedGroupWorker;

/**
 * Created by fabio on 22.01.16.
 */
public interface HomeViewModel extends ViewModel<HomeViewModel.ViewListener>,
        GroupJoinDialogFragment.DialogInteractionListener,
        InvitedGroupWorker.WorkerInteractionListener {

    @Bindable
    boolean isDraftsEmpty();

    void onFabAddPurchaseManualClick(View view);

    void onFabAddPurchaseAutoClick(View view);

    interface ViewListener extends ViewModel.ViewListener {
        void startPurchaseAddActivity(boolean autoMode);
    }
}
