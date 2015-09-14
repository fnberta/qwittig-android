package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.helpers.TaskQueryHelper;
import ch.giantific.qwittig.ui.adapters.TasksRecyclerAdapter;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class TasksFragment extends BaseRecyclerViewFragment implements
        TasksRecyclerAdapter.AdapterInteractionListener {

    public static final String INTENT_TASK_ID = "ch.giantific.qwittig.INTENT_TASK_ID";
    private static final String LOG_TAG = TasksFragment.class.getSimpleName();
    private static final String TASK_QUERY_HELPER = "task_query_helper";

    private Date mDeadlineSelected = new Date(Long.MAX_VALUE);
    private List<ParseObject> mTasks = new ArrayList<>();
    private TasksRecyclerAdapter mRecyclerAdapter;

    public TasksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        findBaseViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new TasksRecyclerAdapter(getActivity(), this, R.layout.row_tasks, mTasks);
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
        TaskQueryHelper taskQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskQueryHelper == null) {
            taskQueryHelper = new TaskQueryHelper();

            fragmentManager.beginTransaction()
                    .add(taskQueryHelper, TASK_QUERY_HELPER)
                    .commit();
        }
    }

    private TaskQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (TaskQueryHelper) fragmentManager.findFragmentByTag(TASK_QUERY_HELPER);
    }

    /**
     * Called from activity when helper fails to pin tasks
     *
     * @param e
     */
    public void onTasksPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeQueryHelper();

        setLoading(false);
    }

    /**
     * Called from activity when helper pinned all tasks
     */
    public void onTasksPinned() {
        updateAdapter();
    }

    /**
     * Called from activity when all tasks queries are finished
     */
    public void onAllTasksQueriesFinished() {
        removeQueryHelper();
        setLoading(false);
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        TaskQueryHelper taskQueryHelper = findQueryHelper(fragmentManager);

        if (taskQueryHelper != null) {
            fragmentManager.beginTransaction().remove(taskQueryHelper).commitAllowingStateLoss();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryTasks(mDeadlineSelected, new LocalQuery.TaskLocalQueryListener() {
            @Override
            public void onTasksLocalQueried(List<ParseObject> tasks) {
                mTasks.clear();

                if (!tasks.isEmpty()) {
                    mTasks.add(null);
                    for (Iterator<ParseObject> iterator = tasks.iterator(); iterator.hasNext(); ) {
                        Task task = (Task) iterator.next();
                        List<ParseUser> usersInvolved = task.getUsersInvolved();
                        if (usersInvolved.isEmpty()) {
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
        });
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility();
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mTasks.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public void onDeadlineSelected(int deadline) {
        if (deadline == R.string.deadline_all) {
            mDeadlineSelected = new Date(Long.MAX_VALUE);
            updateAdapter();
            return;
        }

        Calendar cal = Calendar.getInstance();
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
        GroupCreateDialogFragment groupCreateDialogFragment = new GroupCreateDialogFragment();
        groupCreateDialogFragment.show(getFragmentManager(), "create_group");
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
            Date deadline = task.getDeadline();
            Calendar deadlineNew = DateUtils.getCalendarInstanceUTC();
            deadlineNew.setTime(deadline);
            switch (timeFrame) {
                case Task.TIME_FRAME_DAILY:
                    deadlineNew.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case Task.TIME_FRAME_WEEKLY:
                    deadlineNew.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case Task.TIME_FRAME_MONTHLY:
                    deadlineNew.add(Calendar.MONTH, 1);
                    break;
                case Task.TIME_FRAME_YEARLY:
                    deadlineNew.add(Calendar.YEAR, 1);
                    break;
            }
            task.setDeadline(deadlineNew.getTime());
        }

        List<ParseUser> usersInvolved = task.getUsersInvolved();
        final ParseUser userResponsible = usersInvolved.get(0);
        Collections.rotate(usersInvolved, -1);
        final ParseUser userResponsibleNew = usersInvolved.get(0);

        task.addHistoryEvent();

        task.saveEventually();
        String currentUserId = mCurrentUser.getObjectId();
        if (userResponsible.getObjectId().equals(currentUserId) ||
                userResponsibleNew.getObjectId().equals(currentUserId)) {
            updateAdapter();
        } else {
            mRecyclerAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onRemindButtonClicked(int position) {
        // TODO: send remind push
    }
}
