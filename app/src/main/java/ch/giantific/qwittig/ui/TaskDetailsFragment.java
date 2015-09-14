package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.TaskHistory;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.TaskHistoryRecyclerAdapter;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class TaskDetailsFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener,
        LocalQuery.UserLocalQueryListener {

    private static final String BUNDLE_TASK_ID = "bundle_task_id";
    private FragmentInteractionListener mListener;
    private Task mTask;
    private String mTaskId;
    private User mCurrentUser;
    private Group mCurrentGroup;
    private List<TaskHistory> mTaskHistory = new ArrayList<>();
    private RecyclerView mRecyclerViewHistory;
    private TaskHistoryRecyclerAdapter mRecyclerAdapter;
    private View mEmptyView;
    private ProgressBar mProgressBar;

    public TaskDetailsFragment() {
    }

    public static TaskDetailsFragment newInstance(String taskId) {
        TaskDetailsFragment fragment = new TaskDetailsFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_TASK_ID, taskId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mTaskId = args.getString(BUNDLE_TASK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_details, container, false);

        mRecyclerViewHistory = (RecyclerView) rootView.findViewById(R.id.rv_task_details_history);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_task_details);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerViewHistory.setHasFixedSize(true);
        mRecyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerAdapter = new TaskHistoryRecyclerAdapter(getActivity(),
                R.layout.row_task_details_history, mTaskHistory);
        mRecyclerViewHistory.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }

        queryData();
    }

    public void queryData() {
        // user query instead of fetch because fetch would not include data for the pointers
        LocalQuery.queryTask(mTaskId, this);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        if (object == null) {
            ActivityCompat.startPostponedEnterTransition(getActivity());
            return;
        }

        mTask = (Task) object;

        updateToolbarHeader();
        updateToolbarMenu();

        queryUsers();
    }

    private void queryUsers() {
        LocalQuery.queryUsers(this);
    }

    @Override
    public void onUsersLocalQueried(List<ParseUser> users) {
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
            default:
                timeFrameLocalized = "";
        }

        List<ParseUser> usersInvolved = mTask.getUsersInvolved();
        boolean currentUserIsResponsible = mCurrentUser.getObjectId().equals(
                usersInvolved.get(0).getObjectId());
        String usersString = "";
        if (usersInvolved.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ParseUser parseUser : usersInvolved) {
                User user = (User) parseUser;
                stringBuilder.append(user.getNicknameOrMe(getActivity())).append(" - ");
            }
            // delete last -
            int length = stringBuilder.length();
            stringBuilder.delete(length - 3, length - 1);
            usersString = stringBuilder.toString();
        }

        mListener.setToolbarHeader(title, timeFrameLocalized, usersString, currentUserIsResponsible);
    }

    /**
     * Checks if user is the initator of the task. If yes and task does not contain deleted user,
     * show edit option.
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
        mProgressBar.setVisibility(View.GONE);

        if (!mTaskHistory.isEmpty()) {
            mRecyclerViewHistory.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerViewHistory.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void deleteTask() {
        if (!ParseUtils.isTestUser(ParseUser.getCurrentUser())) {
            mTask.deleteEventually();

            finish(TaskDetailsActivity.RESULT_TASK_DELETED);
        } else {
            showAccountCreateDialog();
        }
    }

    private void finish(int result) {
        Activity activity = getActivity();
        activity.setResult(result);
        activity.finish();
    }

    private void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    public void editTask() {
        Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        intent.putExtra(TasksFragment.INTENT_TASK_ID, mTaskId);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_MODIFY, options.toBundle());
    }

    public void setTaskDone() {
        String timeFrame = mTask.getTimeFrame();

        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME)) {
            // TODO: implement proper deletion
            mTask.deleteEventually();
            finish(TaskDetailsActivity.RESULT_TASK_DELETED);
            return;
        }

        TasksFragment.setTaskDeadline(mTask, timeFrame);

        List<ParseUser> usersInvolved = mTask.getUsersInvolved();
        Collections.rotate(usersInvolved, -1);

        mTask.addHistoryEvent();

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
                        MessageUtils.showBasicSnackbar(mRecyclerViewHistory, getString(R.string.toast_changes_discarded));
                        break;
                    case TaskAddFragment.RESULT_TASK_SAVED:
                        MessageUtils.showBasicSnackbar(mRecyclerViewHistory, getString(R.string.toast_changes_saved));
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

    public interface FragmentInteractionListener {
        void setToolbarHeader(String title, String timeFrame, String usersInvolved,
                              boolean currentUserIsResponsible);

        void showEditOptions(boolean show);
    }
}
