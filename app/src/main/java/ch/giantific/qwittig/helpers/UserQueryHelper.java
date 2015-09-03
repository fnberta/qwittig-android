package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;

/**
 * Created by fabio on 10.12.14.
 */
public class UserQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = UserQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public UserQueryHelper() {
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
            mListener.onAllUserQueriesFinished();
            return;
        }

        mTotalNumberOfQueries = 1;
        queryUsers();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onUsersPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllUserQueriesFinished();
        }
    }

    @Override
    void onUsersPinned() {
        super.onUsersPinned();

        if (mListener != null) {
            mListener.onUsersPinned();
        }

        checkQueryCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onUsersPinned();

        void onUsersPinFailed(ParseException e);

        void onAllUserQueriesFinished();
    }
}
