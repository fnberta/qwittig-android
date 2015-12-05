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
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
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

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.TaskAddActivity;
import ch.giantific.qwittig.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.ui.adapters.TasksRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.workerfragments.query.TaskQueryWorker;
import ch.giantific.qwittig.workerfragments.reminder.TaskRemindWorker;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewOnlineFragment}.
 */
public class TasksFragment extends BaseRecyclerViewOnlineFragment implements
        TasksRecyclerAdapter.AdapterInteractionListener,
        TaskRepository.GetTasksLocalListener {

    public static final String INTENT_TASK_ID = "ch.giantific.qwittig.INTENT_TASK_ID";
    private static final String LOG_TAG = TasksFragment.class.getSimpleName();
    private static final String STATE_TASKS_LOADING = "STATE_TASKS_LOADING";
    private static final String TASK_QUERY_WORKER = "TASK_QUERY_WORKER";
    private static final String TASK_REMIND_WORKER = "TASK_REMIND_WORKER_";
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

        mTaskRepo = new ParseTaskRepository(getActivity());

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

        mRecyclerAdapter = new TasksRecyclerAdapter(getActivity(), mTasks, mCurrentUser, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showErrorSnackbar(R.string.toast_no_connection, getOnlineQueryRetryAction());
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment taskQueryWorker = WorkerUtils.findWorker(fragmentManager, TASK_QUERY_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskQueryWorker == null) {
            taskQueryWorker = new TaskQueryWorker();

            fragmentManager.beginTransaction()
                    .add(taskQueryWorker, TASK_QUERY_WORKER)
                    .commit();
        }
    }

    /**
     * Shows the user an error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown in the process
     */
    public void onTasksUpdatedFailed(@StringRes int errorMessage) {
        showErrorSnackbar(errorMessage, getOnlineQueryRetryAction());
        WorkerUtils.removeWorker(getFragmentManager(), TASK_QUERY_WORKER);

        setLoading(false);
    }

    /**
     * Updates the adapter to reload data from the local data store after new purchases were pinned,
     * removes the retained worker fragment and hides loading indicators.
     */
    public void onTasksUpdated() {
        WorkerUtils.removeWorker(getFragmentManager(), TASK_QUERY_WORKER);
        setLoading(false);

        updateAdapter();
    }

    @Override
    protected void updateAdapter() {
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
                        Snackbar.make(mRecyclerView, R.string.toast_task_deleted,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
            case BaseActivity.INTENT_REQUEST_TASK_NEW:
                switch (resultCode) {
                    case TaskAddFragment.RESULT_TASK_SAVED:
                        Snackbar.make(mRecyclerView, R.string.toast_task_added_new,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case TaskAddFragment.RESULT_TASK_DISCARDED:
                        Snackbar.make(mRecyclerView, R.string.toast_task_discarded,
                                Snackbar.LENGTH_LONG).show();
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

        task.updateDeadline();
        final User userResponsible = task.getUserResponsible();
        final User userResponsibleNew = task.addHistoryEvent(mCurrentUser);
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
            Snackbar.make(mRecyclerView, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        final Task task = (Task) mTasks.get(position);
        final String taskId = task.getObjectId();
        if (mLoadingTasks.contains(taskId)) {
            return;
        }

        setTaskLoading(task, taskId, position, true);
        remindUserWithWorker(taskId);
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

    private void remindUserWithWorker(@NonNull String taskId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment taskRemindWorker = WorkerUtils.findWorker(fragmentManager, getTaskWorkerTag(taskId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskRemindWorker == null) {
            taskRemindWorker = TaskRemindWorker.newInstance(taskId);

            fragmentManager.beginTransaction()
                    .add(taskRemindWorker, TASK_REMIND_WORKER + taskId)
                    .commit();
        }
    }

    @NonNull
    private String getTaskWorkerTag(String taskId) {
        return TASK_REMIND_WORKER + taskId;
    }

    /**
     * Removes the retained worker fragment and disables the loading indicator of the task card.
     * Displays a message to the user that the reminder was sent.
     *
     * @param taskId the object id of the task for which a reminder was sent
     */
    public void onUserReminded(@NonNull String taskId) {
        WorkerUtils.removeWorker(getFragmentManager(), getTaskWorkerTag(taskId));

        Task task = setTaskLoading(taskId, false);
        if (task != null) {
            User userResponsible = (User) task.getUsersInvolved().get(0);
            String nickname = userResponsible.getNickname();
            Snackbar.make(mRecyclerView,
                    getString(R.string.toast_task_reminded_user, nickname), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param taskId       the object id of the task for which an attempt was amde to send a reminder
     * @param errorMessage the error message from the exception thrown in the process
     */
    public void onUserRemindFailed(@NonNull String taskId, @StringRes int errorMessage) {
        Snackbar.make(mRecyclerView, errorMessage, Snackbar.LENGTH_LONG).show();
        WorkerUtils.removeWorker(getFragmentManager(), getTaskWorkerTag(taskId));

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
