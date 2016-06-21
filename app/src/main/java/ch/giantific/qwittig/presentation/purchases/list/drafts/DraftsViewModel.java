/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.StringRes;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.SelectionModeViewModel;

/**
 * Defines an observable view model for the list of drafts screen.
 */
public interface DraftsViewModel extends ListViewModel<Purchase, DraftsViewModel.ViewListener>,
        SelectionModeViewModel<Purchase>, DraftsRecyclerAdapter.AdapterInteractionListener {

    void onReadyForSelectionMode();

    void onDeleteSelectedDraftsClick();

    void onSelectionModeEnded();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);
    }
}
