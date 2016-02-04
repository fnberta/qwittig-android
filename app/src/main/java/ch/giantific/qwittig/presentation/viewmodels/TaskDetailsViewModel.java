/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.domain.models.TaskHistory;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 09.01.16.
 */
public interface TaskDetailsViewModel extends
        ListViewModel<TaskHistory> {

    int RESULT_TASK_DELETED = 2;
    int RESULT_GROUP_CHANGED = 3;
    int TYPE_ITEM = 0;
    int TYPE_HEADER = 1;

    @Bindable
    String getTaskTitle();

    void setTaskTitle(@NonNull String taskTitle);

    @Bindable
    @StringRes
    int getTaskTimeFrame();

    void setTaskTimeFrame(@StringRes int taskTimeFrame);

    @Bindable
    SpannableStringBuilder getTaskUsersInvolved();

    void setTaskUsersInvolved(@NonNull SpannableStringBuilder taskUsersInvolved);

    @Bindable
    boolean isCurrentUserResponsible();

    void setCurrentUserResponsible(boolean currentUserResponsible);

    TaskHistory getTaskHistoryForPosition(int position);

    void deleteTask();

    void editTask();

    void onFabDoneClick(View view);

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
         * @param taskId the object id of the task toedit
         */
        void startEditTaskActivity(@NonNull String taskId);

        /**
         * Builds the users involved string where the current user is in bold style and the rest of
         * the users use normal style
         *
         * @param usersInvolved   the users involved of the task
         * @param userResponsible the user currently responsible for the task
         * @param currentUser     the current user
         * @return a {@link SpannableStringBuilder} with the appropriate string
         */
        SpannableStringBuilder buildUsersInvolvedString(@NonNull List<ParseUser> usersInvolved,
                                                        @NonNull User userResponsible,
                                                        @NonNull User currentUser);

        /**
         * Finishes the task detail screen.
         *
         * @param result the result to pass to the calling screen
         */
        void finishScreen(int result);
    }
}
