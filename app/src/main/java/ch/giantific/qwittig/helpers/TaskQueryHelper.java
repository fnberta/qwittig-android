package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;

/**
 * Created by fabio on 10.12.14.
 */
public class TaskQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = TaskQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public TaskQueryHelper() {
        // empty default constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCurrentGroup == null || mCurrentUserGroups == null) {
            mListener.onAllTaskQueriesFinished();
            return;
        }

        mTotalNumberOfQueries = 1;
        queryTasks();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onTasksPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllTaskQueriesFinished();
        }
    }

    @Override
    void onTasksPinned() {
        super.onTasksPinned();

        if (mListener != null) {
            mListener.onTasksPinned();
        }

        checkQueryCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onTasksPinned();

        void onTasksPinFailed(ParseException e);

        void onAllTaskQueriesFinished();
    }
}
