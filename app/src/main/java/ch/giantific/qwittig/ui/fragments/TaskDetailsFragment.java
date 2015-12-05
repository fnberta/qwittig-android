/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.models.TaskHistory;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.ui.activities.TaskEditActivity;
import ch.giantific.qwittig.ui.adapters.TaskHistoryRecyclerAdapter;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Shows the details of a {@link Task}. Most of the information gets displayed in the
 * {@link Toolbar} of the hosting {@link Activity}. The fragment itself shows a list of users that
 * have previously finished the task.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskDetailsFragment extends BaseFragment implements
        TaskRepository.GetTaskLocalListener,
        UserRepository.GetUsersLocalListener {

    private static final String BUNDLE_TASK_ID = "BUNDLE_TASK_ID";
    private FragmentInteractionListener mListener;
    private Task mTask;
    private String mTaskId;
    @NonNull
    private List<TaskHistory> mTaskHistory = new ArrayList<>();
    private RecyclerView mRecyclerViewHistory;
    private TaskHistoryRecyclerAdapter mRecyclerAdapter;
    private View mEmptyView;
    private ContentLoadingProgressBar mProgressBar;
    private TaskRepository mTaskRepo;
    private UserRepository mUserRepo;

    public TaskDetailsFragment() {
    }

    /**
     * Returns a new instance of {@link TaskDetailsFragment}.
     *
     * @param taskId the object id of the task for which the details should be displayed
     * @return a new instance of {@link TaskDetailsFragment}
     */
    @NonNull
    public static TaskDetailsFragment newInstance(String taskId) {
        TaskDetailsFragment fragment = new TaskDetailsFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_TASK_ID, taskId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = getActivity();
        mTaskRepo = new ParseTaskRepository(activity);
        mUserRepo = new ParseUserRepository(activity);

        Bundle args = getArguments();
        if (args != null) {
            mTaskId = args.getString(BUNDLE_TASK_ID, "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = view.findViewById(R.id.empty_view);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.pb_base);

        mRecyclerViewHistory = (RecyclerView) view.findViewById(R.id.rv_task_details_history);
        mRecyclerViewHistory.setHasFixedSize(true);
        mRecyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerAdapter = new TaskHistoryRecyclerAdapter(getActivity(), mTaskHistory);
        mRecyclerViewHistory.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateCurrentUserAndGroup();
        if (mCurrentGroup != null) {
            queryData();
        } else {
            Snackbar.make(mRecyclerViewHistory, R.string.toast_error_purchase_details_group_not,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Queries the local data store for the data of the task for which to show details for.
     * <p/>
     * Uses a  query instead of a fetchFromLocalDatastore because this would not include the data
     * for the pointers.
     */
    public void queryData() {
        mTaskRepo.getTaskLocalAsync(mTaskId, this);
    }

    @Override
    public void onTaskLocalLoaded(@NonNull Task task) {
        mTask = task;

        updateToolbarHeader();
        updateToolbarMenu();

        queryUsers();
    }

    private void queryUsers() {
        mUserRepo.getUsersLocalAsync(mCurrentGroup, this);
    }

    @Override
    public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
        mTaskHistory.clear();

        Map<String, List<Date>> taskHistory = mTask.getHistory();
        Set<String> keys = taskHistory.keySet();
        for (ParseUser user : users) {
            String userId = user.getObjectId();
            if (keys.contains(userId)) {
                List<Date> dates = taskHistory.get(userId);
                for (Date date : dates) {
                    mTaskHistory.add(new TaskHistory(user, date));
                }
            }
        }

        Collections.sort(mTaskHistory, Collections.reverseOrder());
        mRecyclerAdapter.notifyDataSetChanged();

        toggleMainVisibility();
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    private void updateToolbarHeader() {
        String title = mTask.getTitle();
        String timeFrame = mTask.getTimeFrame();
        String timeFrameLocalized;
        switch (timeFrame) {
            case Task.TIME_FRAME_DAILY:
                timeFrameLocalized = getString(R.string.time_frame_daily);
                break;
            case Task.TIME_FRAME_WEEKLY:
                timeFrameLocalized = getString(R.string.time_frame_weekly);
                break;
            case Task.TIME_FRAME_MONTHLY:
                timeFrameLocalized = getString(R.string.time_frame_monthly);
                break;
            case Task.TIME_FRAME_YEARLY:
                timeFrameLocalized = getString(R.string.time_frame_yearly);
                break;
            case Task.TIME_FRAME_AS_NEEDED:
                timeFrameLocalized = getString(R.string.time_frame_as_needed);
                break;
            case Task.TIME_FRAME_ONE_TIME:
                timeFrameLocalized = getString(R.string.time_frame_one_time);
                break;
            default:
                timeFrameLocalized = "";
        }

        List<ParseUser> usersInvolved = mTask.getUsersInvolved();
        User userResponsible = (User) usersInvolved.get(0);
        boolean currentUserIsResponsible = mCurrentUser.getObjectId().equals(
                usersInvolved.get(0).getObjectId());

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        int usersInvolvedSize = usersInvolved.size();
        stringBuilder.append(userResponsible.getNicknameOrMe(getActivity(), mCurrentUser));

        if (usersInvolvedSize > 1) {
            int spanEnd = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            for (int i = 1; i < usersInvolvedSize; i++) {
                User user = (User) usersInvolved.get(i);
                stringBuilder.append(" - ").append(user.getNicknameOrMe(getActivity(), mCurrentUser));
            }
        }

        mListener.setToolbarHeader(title, timeFrameLocalized, stringBuilder, currentUserIsResponsible);
    }

    /**
     * Checks if user is the initiator of the task. If yes and task does not contain deleted user,
     * shows edit option in the action bar of the hosting {@link Activity}.
     */
    private void updateToolbarMenu() {
        User initiator = mTask.getInitiator();
        boolean showEditOptions = initiator.getObjectId().equals(mCurrentUser.getObjectId());

        if (showEditOptions) {
            List<ParseUser> usersInvolved = mTask.getUsersInvolved();
            for (ParseUser parseUser : usersInvolved) {
                User user = (User) parseUser;
                if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                    showEditOptions = false;
                    break;
                }
            }
        }

        mListener.showEditOptions(showEditOptions);
    }

    private void toggleMainVisibility() {
        mProgressBar.hide();

        if (!mTaskHistory.isEmpty()) {
            mRecyclerViewHistory.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerViewHistory.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Deletes the task if the user is not a test user and finishes.
     */
    public void deleteTask() {
        if (!ParseUtils.isTestUser(mCurrentUser)) {
            mTask.deleteEventually();

            finish(TaskDetailsActivity.RESULT_TASK_DELETED);
        } else {
            mListener.showAccountCreateDialog();
        }
    }

    private void finish(int result) {
        Activity activity = getActivity();
        activity.setResult(result);
        activity.finish();
    }

    /**
     * Starts {@link TaskEditActivity} to edit the task.
     */
    public void editTask() {
        Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        intent.putExtra(TasksFragment.INTENT_TASK_ID, mTaskId);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_MODIFY, options.toBundle());
    }

    /**
     * Sets the task to finished for the current user and sets the next user involved in line as
     * responsible.
     * <p/>
     * If task was a one-time thing, deletes it.
     * <p/>
     * TODO: send notification to the group
     */
    public void setTaskDone() {
        String timeFrame = mTask.getTimeFrame();

        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME)) {
            mTask.deleteEventually();
            finish(TaskDetailsActivity.RESULT_TASK_DELETED);
            return;
        }

        mTask.updateDeadline();
        mTask.addHistoryEvent(mCurrentUser);
        mTask.saveEventually();

        updateToolbarHeader();
        queryUsers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BaseActivity.INTENT_REQUEST_TASK_MODIFY:
                switch (resultCode) {
                    case TaskEditFragment.RESULT_TASK_DISCARDED:
                        Snackbar.make(mRecyclerViewHistory, R.string.toast_changes_discarded,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case TaskAddFragment.RESULT_TASK_SAVED:
                        Snackbar.make(mRecyclerViewHistory, R.string.toast_changes_saved,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Sets what to show in the {@link Toolbar} of the hosting {@link Activity}.
         *
         * @param title                    the title to show
         * @param timeFrame                the time frame to show
         * @param usersInvolved            the users involved to show
         * @param currentUserIsResponsible whether it's the current user's turn to finish the task
         */
        void setToolbarHeader(@NonNull String title, @NonNull String timeFrame,
                              @NonNull SpannableStringBuilder usersInvolved,
                              boolean currentUserIsResponsible);

        /**
         * Sets whether the edit option for the task should be shown in the action bar menu of the
         * hosting {@link Activity}.
         *
         * @param show whether to show the edit option or not
         */
        void showEditOptions(boolean show);
    }
}
