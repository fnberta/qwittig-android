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
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItem;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItemModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;

/**
 * Defines an observable view model for the task list screen.
 */
public interface AssignmentsViewModel extends ListViewModel<AssignmentItemModel, AssignmentsViewModel.ViewListener> {

    void onAssignmentRowClick(@NonNull AssignmentItem itemModel);

    void onDoneButtonClick(@NonNull AssignmentItem itemModel);

    void onRemindButtonClick(@NonNull AssignmentItem itemModel);

    void onAddAssignmentFabClick(View view);

    void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onAssignmentDeleted(@NonNull String assignmentId);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        String buildUpNextString(@NonNull List<Identity> identities);

        String buildDeadlineString(@StringRes int res, Object... args);
    }
}
