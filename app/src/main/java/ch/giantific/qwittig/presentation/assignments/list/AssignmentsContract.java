/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.AssignmentItemViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.BaseAssignmentItemViewModel;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.SortedListView;

/**
 * Defines an observable view model for the task list screen.
 */
public interface AssignmentsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<BaseAssignmentItemViewModel> {

        AssignmentDeadline[] getAssignmentDeadlines();

        void onAssignmentRowClick(@NonNull AssignmentItemViewModel itemViewModel);

        void onDoneButtonClick(@NonNull AssignmentItemViewModel itemViewModel);

        void onRemindButtonClick(@NonNull AssignmentItemViewModel itemViewModel);

        void onAddAssignmentClick(View view);

        void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onAssignmentDeleted(@NonNull String assignmentId);
    }

    interface ViewListener extends BaseView, SortedListView<BaseAssignmentItemViewModel> {
        String buildUpNextString(@NonNull List<Identity> identities);

        String buildDeadlineString(@StringRes int res, Object... args);
    }
}
