/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHeaderItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHistoryItemViewModel;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for the task details screen.
 */
public interface AssignmentDetailsContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void onDeleteAssignmentMenuClick();

        void onEditAssignmentMenuClick();

        void onDoneClick(View view);
    }

    interface ViewListener extends BaseView {

        /**
         * Starts the postponed enter transition.
         */
        void startPostponedEnterTransition();

        /**
         * Builds the users involved string where the current user is in bold style and the rest of
         * the users use normal style
         *
         * @param identities the identities of the task
         * @return a {@link SpannableStringBuilder} with the appropriate string
         */
        SpannableStringBuilder buildIdentitiesString(@NonNull List<Identity> identities);

        void addItem(@NonNull AssignmentDetailsHeaderItemViewModel itemViewModel);

        void addItems(@NonNull List<AssignmentDetailsHistoryItemViewModel> itemViewModels);

        void clearItems();

        void notifyItemsChanged();
    }

    @IntDef({AssignmentDetailsResult.DELETED, AssignmentDetailsResult.GROUP_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface AssignmentDetailsResult {
        int DELETED = 2;
        int GROUP_CHANGED = 3;
    }
}
