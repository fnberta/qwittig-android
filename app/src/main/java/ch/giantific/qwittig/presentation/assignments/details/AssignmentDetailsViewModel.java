/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;

/**
 * Defines an observable view model for the task details screen.
 */
public interface AssignmentDetailsViewModel extends ListViewModel<AssignmentDetailsItemModel, AssignmentDetailsViewModel.ViewListener> {

    @Bindable
    String getTitle();

    void setTitle(@NonNull String title);

    @Bindable
    @StringRes
    int getTimeFrame();

    void setTimeFrame(@StringRes int timeFrame);

    @Bindable
    SpannableStringBuilder getIdentitiesText();

    void setIdentitiesText(@NonNull SpannableStringBuilder identitiesText);

    @Bindable
    boolean isResponsible();

    void setResponsible(boolean responsible);

    void onDeleteAssignmentMenuClick();

    void onEditAssignmentMenuClick();

    void onFabDoneClick(View view);

    @IntDef({AssignmentDetailsResult.DELETED, AssignmentDetailsResult.GROUP_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface AssignmentDetailsResult {
        int DELETED = 2;
        int GROUP_CHANGED = 3;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

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
    }
}
