/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.SelectionModeViewModel;

/**
 * Defines an observable view model for the list of drafts screen.
 */
public interface DraftsViewModel extends ListViewModel<Purchase>,
        SelectionModeViewModel<Purchase>, DraftsRecyclerAdapter.AdapterInteractionListener {

    void onReadyForSelectionMode();

    void onDeleteSelectedDraftsClick();

    void onSelectionModeEnded();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void startPurchaseEditActivity(@NonNull Purchase draft);

        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);
    }
}
