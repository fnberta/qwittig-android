/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.presenters.SelectionModePresenter;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;

/**
 * Defines an observable view model for the list of drafts screen.
 */
public interface DraftsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<DraftItemViewModel>,
            SelectionModePresenter {

        void onDeleteSelectedDraftsClick();

        void onSelectionModeEnded();

        void onDraftDeleted(@NonNull String draftId);

        void onDraftRowClick(@NonNull DraftItemViewModel itemViewModel);

        boolean onDraftRowLongClick(@NonNull DraftItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseView,
            SortedListView<DraftItemViewModel> {

        void startSelectionMode();

        void stopSelectionMode();

        void setSelectionModeTitle(@StringRes int title, int draftsSelected);

        int getItemCount();

        void notifyItemChanged(int position);

        void scrollToItemPosition(int position);
    }
}
