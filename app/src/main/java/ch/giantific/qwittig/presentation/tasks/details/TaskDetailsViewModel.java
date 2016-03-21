/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

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
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsBaseItem;

/**
 * Defines an observable view model for the task details screen.
 */
public interface TaskDetailsViewModel extends
        ListViewModel<TaskDetailsBaseItem> {

    @Bindable
    String getTaskTitle();

    @Bindable
    @StringRes
    int getTaskTimeFrame();

    void setTaskTimeFrame(@StringRes int taskTimeFrame);

    @Bindable
    SpannableStringBuilder getTaskIdentities();

    void setTaskIdentities(@NonNull SpannableStringBuilder taskUsersInvolved);

    @Bindable
    boolean isCurrentUserResponsible();

    void setCurrentUserResponsible(boolean currentUserResponsible);

    void deleteTask();

    void editTask();

    void onFabDoneClick(View view);

    @IntDef({TaskDetailsResult.TASK_DELETED, TaskDetailsResult.GROUP_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface TaskDetailsResult {
        int TASK_DELETED = 2;
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
         * Toggles the display of the edit options in the toolbar.
         *
         * @param showOptions whether to who the options or not
         */
        void toggleEditOptions(boolean showOptions);

        /**
         * Starts the screen that allows the user to edit a task
         *
         * @param taskId the object id of the task to edit
         */
        void startEditTaskActivity(@NonNull String taskId);

        /**
         * Builds the users involved string where the current user is in bold style and the rest of
         * the users use normal style
         *
         * @param identities          the identities of the task
         * @param identityResponsible the identity currently responsible for the task
         * @return a {@link SpannableStringBuilder} with the appropriate string
         */
        SpannableStringBuilder buildTaskIdentitiesString(@NonNull List<Identity> identities,
                                                         @NonNull Identity identityResponsible);

        /**
         * Finishes the task detail screen.
         *
         * @param result the result to pass to the calling screen
         */
        void finishScreen(int result);
    }
}
