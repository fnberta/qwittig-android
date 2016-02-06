/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.SelectionModeViewModel;

/**
 * Created by fabio on 21.01.16.
 */
public interface HomeDraftsViewModel extends ListViewModel<Purchase>,
        SelectionModeViewModel<Purchase>, DraftsRecyclerAdapter.AdapterInteractionListener {

    void onReadyForSelectionMode();

    void onDeleteSelectedDraftsClick();

    void onSelectionModeEnded();

    interface ViewListener extends ListViewModel.ViewListener {
        void startPurchaseEditActivity(@NonNull Purchase draft);

        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);
    }
}
