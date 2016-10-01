/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.common.presenters.SelectionModePresenter;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;

/**
 * Defines an observable view model for the list of drafts screen.
 */
public interface DraftsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<DraftItemViewModel>,
            SelectionModePresenter {

        DraftsViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void onDeleteSelectedDraftsClick();

        void onSelectionModeEnded();

        void onDraftDeleted(@NonNull String draftId);

        void onDraftRowClick(@NonNull DraftItemViewModel itemViewModel);

        boolean onDraftRowLongClick(@NonNull DraftItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseViewListener {
        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);
    }
}
