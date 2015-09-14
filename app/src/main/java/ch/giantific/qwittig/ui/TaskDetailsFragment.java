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

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
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

    private void queryData() {
        // user query instead of fetch because fetch would not include data for the pointers
        LocalQuery.queryTask(mTaskId, this);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        mTask = (Task) object;

        updateToolbarTitle();
        checkAllUsersValid();

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

        mRecyclerAdapter.notifyDataSetChanged();

        toggleMainViewVisibility();
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    private void updateToolbarTitle() {
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

        mListener.setToolbarTitleTimeFrame(title, timeFrameLocalized);
    }

    /**
     * Checks if task contains deleted user, if yes don't offer edit option
     */
    private void checkAllUsersValid() {
        List<ParseUser> usersInvolved = mTask.getUsersInvolved();
        boolean allUsersAreValid = true;

        for (ParseUser parseUser : usersInvolved) {
            User user = (User) parseUser;
            if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                allUsersAreValid = false;
                break;
            }
        }

        mListener.showEditOption(allUsersAreValid);
    }

    private void toggleMainViewVisibility() {
//        boolean purchaseIsNull = mPurchase == null;
//        mRecyclerView.setVisibility(purchaseIsNull ? View.GONE : View.VISIBLE);
//        mProgressBar.setVisibility(purchaseIsNull ? View.VISIBLE : View.GONE);
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
        void setToolbarTitleTimeFrame(String title, String timeFrame);

        void showEditOption(boolean show);
    }
}
