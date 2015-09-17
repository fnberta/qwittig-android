package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.TaskUser;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class TaskEditFragment extends TaskAddFragment {

    private static final String BUNDLE_EDIT_TASK_ID = "edit_task_id";
    private static final String STATE_ITEMS_SET = "items_set";
    private static final String STATE_OLD_TITLE = "state_old_title";
    private static final String STATE_OLD_TIME_FRAME = "state_old_time_frame";
    private static final String STATE_OLD_DEADLINE = "state_old_deadline";
    private static final String LOG_TAG = TaskEditFragment.class.getSimpleName();
    private boolean mOldValuesSet;
    private Task mEditTask;
    private String mEditTaskId;
    private String mOldTitle;
    private String mOldTimeFrame;
    private Date mOldDeadline;
    private List<ParseUser> mOldUsersInvolved;

    public static TaskEditFragment newInstance(String taskId) {
        TaskEditFragment fragment = new TaskEditFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EDIT_TASK_ID, taskId);
        fragment.setArguments(args);

        return fragment;
    }

    public TaskEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mEditTaskId = args.getString(BUNDLE_EDIT_TASK_ID);
        }

        if (savedInstanceState != null) {
            mOldValuesSet = savedInstanceState.getBoolean(STATE_ITEMS_SET, false);
            mOldTitle = savedInstanceState.getString(STATE_OLD_TITLE);
            mOldTimeFrame = savedInstanceState.getString(STATE_OLD_TIME_FRAME);
            mOldDeadline = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_OLD_DEADLINE));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
        outState.putString(STATE_OLD_TITLE, mOldTitle);
        outState.putString(STATE_OLD_TIME_FRAME, mOldTimeFrame);
        outState.putLong(STATE_OLD_DEADLINE, DateUtils.parseDateToLong(mOldDeadline));
    }

    @Override
    void setupUserList() {
        fetchOldTask();
    }

    private void fetchOldTask() {
        LocalQuery.fetchObjectFromId(Task.CLASS, mEditTaskId, new LocalQuery.ObjectLocalFetchListener() {
            @Override
            public void onObjectFetched(ParseObject object) {
                mEditTask = (Task) object;

                if (!mOldValuesSet) {
                    restoreOldValues();
                }

                queryUsers();
            }
        });
    }

    private void restoreOldValues() {
        mOldTitle = mEditTask.getTitle();
        mListener.setTaskTitle(mOldTitle);
        mOldTimeFrame = mEditTask.getTimeFrame();
        setTimeFrame(mOldTimeFrame);
        if (!mOldTimeFrame.equals(Task.TIME_FRAME_AS_NEEDED)) {
            mOldDeadline = mEditTask.getDeadline();
            setDeadline(mOldDeadline);
        }
        mOldUsersInvolved = mEditTask.getUsersInvolved();
        setUsersInvolved(mOldUsersInvolved);

        mOldValuesSet = true;
        mUsersRecyclerAdapter.notifyDataSetChanged();
    }

    private void setTimeFrame(@Task.TimeFrame String timeFrame) {
        int res = 0;

        switch (timeFrame) {
            case Task.TIME_FRAME_ONE_TIME:
                res = R.string.time_frame_one_time;
                break;
            case Task.TIME_FRAME_DAILY:
                res = R.string.time_frame_daily;
                break;
            case Task.TIME_FRAME_WEEKLY:
                res = R.string.time_frame_weekly;
                break;
            case Task.TIME_FRAME_MONTHLY:
                res = R.string.time_frame_monthly;
                break;
            case Task.TIME_FRAME_YEARLY:
                res = R.string.time_frame_yearly;
                break;
            case Task.TIME_FRAME_AS_NEEDED:
                res = R.string.time_frame_as_needed;
                break;
        }

        if (res > 0) {
            int position = mTimeFrameAdapter.getPosition(res);
            mSpinnerTimeFrame.setSelection(position);
        }
    }

    private void setUsersInvolved(List<ParseUser> usersInvolved) {
        for (ParseUser user : usersInvolved) {
            mUsersInvolved.add(new TaskUser(user.getObjectId(), true));
        }
    }

    @Override
    boolean changesWereMade() {
        if (!mOldTitle.equals(mListener.getTaskTitle()) ||
                !mOldTimeFrame.equals(getTimeFrameSelected()) ||
                mOldDeadline.compareTo(mDeadlineSelected) != 0) {
            return true;
        }

        if (mOldUsersInvolved == null) {
            mOldUsersInvolved = mEditTask.getUsersInvolved();
        }

        int oldUsersInvolvedSize = mOldUsersInvolved.size();
        List<ParseUser> newUsersInvolved = getUsersInvolved();
        if (oldUsersInvolvedSize != newUsersInvolved.size()) {
            return true;
        }

        for (int i = 0; i < oldUsersInvolvedSize; i++) {
            ParseUser userOld = mOldUsersInvolved.get(i);
            ParseUser userNew = newUsersInvolved.get(i);

            if (!userOld.getObjectId().equals(userNew.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    @Override
    Task getTask(String title, String timeFrame, List<ParseUser> usersInvolved) {
        mEditTask.setTitle(title);
        mEditTask.setTimeFrame(timeFrame);
        mEditTask.setDeadline(mDeadlineSelected);
        mEditTask.setUsersInvolved(usersInvolved);
        return mEditTask;
    }
}
