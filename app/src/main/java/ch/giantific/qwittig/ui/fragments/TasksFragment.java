/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.data.helpers.query.TaskQueryHelper;
import ch.giantific.qwittig.data.helpers.reminder.TaskRemindHelper;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.TaskAddActivity;
import ch.giantific.qwittig.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.ui.adapters.TasksRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewFragment}.
 */
public class TasksFragment extends BaseRecyclerViewFragment implements
        TasksRecyclerAdapter.AdapterInteractionListener,
        TaskRepository.GetTasksLocalListener {

    public static final String INTENT_TASK_ID = "ch.giantific.qwittig.INTENT_TASK_ID";
    private static final String LOG_TAG = TasksFragment.class.getSimpleName();
    private static final String STATE_TASKS_LOADING = "STATE_TASKS_LOADING";
    private static final String TASK_QUERY_HELPER = "TASK_QUERY_HELPER";
    private static final String TASK_REMIND_HELPER = "TASK_REMIND_HELPER_";
    private static final String CREATE_GROUP_DIALOG = "CREATE_GROUP_DIALOG";

    private FragmentInteractionListener mListener;
    @NonNull
    private Date mDeadlineSelected = new Date(Long.MAX_VALUE);
    @NonNull
    private List<ParseObject> mTasks = new ArrayList<>();
    private TasksRecyclerAdapter mRecyclerAdapter;
    private ArrayList<String> mLoadingTasks;
    private TaskRepository mTaskRepo;

    public TasksFragment() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTaskRepo = new ParseTaskRepository();

        if (savedInstanceState != null) {
            ArrayList<String> loadingTasks = savedInstanceState.getStringArrayList(STATE_TASKS_LOADING);
            mLoadingTasks = loadingTasks != null ? loadingTasks : new ArrayList<String>();
        } else {
            mLoadingTasks = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_TASKS_LOADING, mLoadingTasks);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new TasksRecyclerAdapter(getActivity(), mTasks, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment taskQueryHelper = HelperUtils.findHelper(fragmentManager, TASK_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskQueryHelper == null) {
            taskQueryHelper = new TaskQueryHelper();

            fragmentManager.beginTransaction()
                    .add(taskQueryHelper, TASK_QUERY_HELPER)
                    .commit();
        }
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onTasksUpdatedFailed(int errorCode) {
        ParseErrorHandler.handleParseError(getActivity(), errorCode);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), errorCode));
        HelperUtils.removeHelper(getFragmentManager(), TASK_QUERY_HELPER);

        setLoading(false);
    }

    /**
     * Updates the adapter to reload data from the local data store after new purchases were pinned,
     * removes the retained helper fragment and hides loading indicators.
     */
    public void onTasksUpdated() {
        HelperUtils.removeHelper(getFragmentManager(), TASK_QUERY_HELPER);
        setLoading(false);

        updateAdapter();
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        mTaskRepo.getTasksLocalAsync(mCurrentGroup, mDeadlineSelected, this);
    }

    @Override
    public void onTasksLocalLoaded(@NonNull List<ParseObject> tasks) {
        mTasks.clear();

        if (!tasks.isEmpty()) {
            mTasks.add(null);
            for (Iterator<ParseObject> iterator = tasks.iterator(); iterator.hasNext(); ) {
                Task task = (Task) iterator.next();
                task.setLoading(mLoadingTasks.contains(task.getObjectId()));

                List<ParseUser> usersInvolved = task.getUsersInvolved();
                if (usersInvolved.isEmpty() || !usersInvolved.contains(mCurrentUser)) {
                    iterator.remove();
                } else {
                    ParseUser userResponsible = usersInvolved.get(0);
                    if (mCurrentUser.getObjectId().equals(userResponsible.getObjectId())) {
                        mTasks.add(task);
                        iterator.remove();
                    }
                }
            }

            mTasks.add(null);
            mTasks.addAll(tasks);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.notifyDataSetChanged();
        showMainView();
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mTasks.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Sets what tasks are shown in the list depending on the deadline selected
     *
     * @param deadline the type of deadline selected: all, today, this week, this month or this year
     */
    public void onDeadlineSelected(int deadline) {
        if (deadline == R.string.deadline_all) {
            mDeadlineSelected = new Date(Long.MAX_VALUE);
            updateAdapter();
            return;
        }

        Calendar cal = DateUtils.getCalendarInstanceUTC();
        switch (deadline) {
            case R.string.deadline_today: {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            }
            case R.string.deadline_week: {
                int firstDayOfWeek = cal.getFirstDayOfWeek();
                cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            }
            case R.string.deadline_month: {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, 1);
                break;
            }
            case R.string.deadline_year: {
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.add(Calendar.YEAR, 1);
                break;
            }
        }

        cal = DateUtils.resetToMidnight(cal);
        mDeadlineSelected = cal.getTime();
        updateAdapter();
    }

    /**
     * Starts {@link TaskAddActivity} to let the user add a new {@link Task}.
     */
    public void addNewTask() {
        if (userIsInGroup()) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, TaskAddActivity.class);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
            startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_NEW, options.toBundle());
        }
    }

    private boolean userIsInGroup() {
        if (mCurrentUser == null) {
            return false;
        }

        if (mCurrentGroup == null) {
            showCreateGroupDialog();
            return false;
        }

        return true;
    }

    private void showCreateGroupDialog() {
        GroupCreateDialogFragment groupCreateDialogFragment = GroupCreateDialogFragment.newInstance(R.string.dialog_group_create_tasks);
        groupCreateDialogFragment.show(getFragmentManager(), CREATE_GROUP_DIALOG);
    }

    @Override
    public void onTaskRowClicked(int position) {
        Task task = (Task) mTasks.get(position);
        Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra(INTENT_TASK_ID, task.getObjectId());
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_DETAILS, options.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BaseActivity.INTENT_REQUEST_TASK_DETAILS:
                switch (resultCode) {
                    case TaskDetailsActivity.RESULT_TASK_DELETED:
                        MessageUtils.showBasicSnackbar(mRecyclerView,
                                getString(R.string.toast_task_deleted));
                        break;
                }
                break;
            case BaseActivity.INTENT_REQUEST_TASK_NEW:
                switch (resultCode) {
                    case TaskAddFragment.RESULT_TASK_SAVED:
                        MessageUtils.showBasicSnackbar(mRecyclerView,
                                getString(R.string.toast_task_added_new));
                        break;
                    case TaskAddFragment.RESULT_TASK_DISCARDED:
                        MessageUtils.showBasicSnackbar(mRecyclerView,
                                getString(R.string.toast_task_discarded));
                        break;
                }
                break;
        }
    }

    @Override
    public void onDoneButtonClicked(int position) {
        Task task = (Task) mTasks.get(position);
        String timeFrame = task.getTimeFrame();

        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME)) {
            task.deleteEventually();
            mTasks.remove(task);
            mRecyclerAdapter.notifyItemRemoved(position);
            return;
        }

        if (!timeFrame.equals(Task.TIME_FRAME_AS_NEEDED)) {
            task.updateDeadline(timeFrame);
        }

        final ParseUser userResponsible = task.getUserResponsible();
        final ParseUser userResponsibleNew = task.addHistoryEvent();
        task.saveEventually();

        String currentUserId = mCurrentUser.getObjectId();
        if (userResponsible != null && userResponsible.getObjectId().equals(currentUserId) ||
                userResponsibleNew != null &&
                        userResponsibleNew.getObjectId().equals(currentUserId)) {
            updateAdapter();
        } else {
            mRecyclerAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onRemindButtonClicked(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_no_connection));
            return;
        }

        final Task task = (Task) mTasks.get(position);
        final String taskId = task.getObjectId();
        if (mLoadingTasks.contains(taskId)) {
            return;
        }

        setTaskLoading(task, taskId, position, true);
        remindUserWithHelper(taskId);
    }

    @Nullable
    private Task setTaskLoading(@NonNull String objectId, boolean isLoading) {
        Task taskLoading = null;

        for (int i = 0, tasksSize = mTasks.size(); i < tasksSize; i++) {
            Task task = (Task) mTasks.get(i);
            if (task != null && objectId.equals(task.getObjectId())) {
                setTaskLoading(task, objectId, i, isLoading);
                taskLoading = task;
            }
        }

        return taskLoading;
    }

    private void setTaskLoading(@NonNull Task task, String objectId, int position,
                                boolean isLoading) {
        task.setLoading(isLoading);
        mRecyclerAdapter.notifyItemChanged(position);

        if (isLoading) {
            mLoadingTasks.add(objectId);
        } else {
            mLoadingTasks.remove(objectId);
        }
    }

    private void remindUserWithHelper(@NonNull String taskId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment taskRemindHelper = HelperUtils.findHelper(fragmentManager, getTaskHelperTag(taskId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskRemindHelper == null) {
            taskRemindHelper = TaskRemindHelper.newInstance(taskId);

            fragmentManager.beginTransaction()
                    .add(taskRemindHelper, TASK_REMIND_HELPER + taskId)
                    .commit();
        }
    }

    @NonNull
    private String getTaskHelperTag(String taskId) {
        return TASK_REMIND_HELPER + taskId;
    }

    /**
     * Removes the retained helper fragment and disables the loading indicator of the task card.
     * Displays a message to the user that the reminder was sent.
     *
     * @param taskId the object id of the task for which a reminder was sent
     */
    public void onUserReminded(@NonNull String taskId) {
        HelperUtils.removeHelper(getFragmentManager(), getTaskHelperTag(taskId));

        Task task = setTaskLoading(taskId, false);
        if (task != null) {
            User userResponsible = (User) task.getUsersInvolved().get(0);
            String nickname = userResponsible.getNickname();
            MessageUtils.showBasicSnackbar(mRecyclerView,
                    getString(R.string.toast_task_reminded_user, nickname));
        }
    }

    /**
     * Passes the error codeto the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param taskId    the object id of the task for which an attempt was amde to send a reminder
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onUserRemindFailed(@NonNull String taskId, int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        MessageUtils.showBasicSnackbar(mRecyclerView,
                ParseErrorHandler.getErrorMessage(context, errorCode));
        HelperUtils.removeHelper(getFragmentManager(), getTaskHelperTag(taskId));

        setTaskLoading(taskId, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Currently a stub.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
    }
}
