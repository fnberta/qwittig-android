/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.SelectionModeViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftItemModel;

/**
 * Defines an observable view model for the list of drafts screen.
 */
public interface DraftsViewModel extends ListViewModel<DraftItemModel, DraftsViewModel.ViewListener>,
        SelectionModeViewModel<DraftItemModel> {

    void onDeleteSelectedDraftsClick();

    void onSelectionModeEnded();

    void onDraftDeleted(@NonNull String draftId);

    void onDraftRowClick(@NonNull DraftItemModel itemModel);

    boolean onDraftRowLongClick(@NonNull DraftItemModel itemModel);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);
    }
}
