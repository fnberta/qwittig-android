package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
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
    private List<ParseUser> mUsersQueried;

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
    void setupStartUserLists(List<ParseUser> users, int usersSize) {
        mUsersQueried = users;
        // do nothing else, users lists will be setup when old task is fetched
    }

    @Override
    void onUsersAvailableReady() {
        fetchOldTask();
    }

    private void fetchOldTask() {
        LocalQuery.fetchObjectFromId(Task.CLASS, mEditTaskId, new LocalQuery.ObjectLocalFetchListener() {
            @Override
            public void onObjectFetched(ParseObject object) {
                mEditTask = (Task) object;

                if (!mOldValuesSet) {
                    restoreOldValues();
                } else {
                    mUsersRecyclerAdapter.notifyDataSetChanged();
                }
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
        setUsers(mOldUsersInvolved);

        mOldValuesSet = true;
        mUsersRecyclerAdapter.notifyDataSetChanged();
    }

    private void setTimeFrame(@Task.TimeFrame String timeFrame) {
        int res = 0;

        switch (timeFrame) {
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

    private void setUsers(List<ParseUser> usersInvolved) {
        for (ParseUser user : usersInvolved) {
            mUsersAvailable.add(user);
            mUsersInvolved.add(user.getObjectId());
        }

        for (ParseUser user : mUsersQueried) {
            if (!mUsersAvailable.contains(user)) {
                mUsersAvailable.add(user);
            }
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
        if (oldUsersInvolvedSize != mUsersInvolved.size()) {
            return true;
        }

        for (int i = 0; i < oldUsersInvolvedSize; i++) {
            User userOld = (User) mOldUsersInvolved.get(i);
            User userNew = (User) mUsersAvailable.get(i);

            if (!userOld.getObjectId().equals(userNew.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    @Override
    Task getTask(String title) {
        mEditTask.setTitle(title);
        mEditTask.setTimeFrame(getTimeFrameSelected());
        mEditTask.setDeadline(mDeadlineSelected);
        mEditTask.setUsersInvolved(getUsersFromId(mUsersInvolved));
        return mEditTask;
    }
}
