package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;

/**
 * Created by fabio on 10.12.14.
 */
public class CompensationQueryHelper extends BaseQueryHelper {

    public static final String COMPENSATION_QUERY_HELPER = "compensation_query_helper";
    private static final String LOG_TAG = CompensationQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public CompensationQueryHelper() {
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
            mListener.onAllQueriesFinished();
            return;
        }

        // compensationsPaid for each group + compensationsUnpaid
        mTotalNumberOfQueries = mCurrentUserGroups.size() + 1;

        queryCompensations();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onCompensationsPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllQueriesFinished();
        }
    }

    @Override
    void onCompensationsUnpaidPinned() {
        super.onCompensationsUnpaidPinned();

        checkQueryCount();

        if (mListener != null) {
            mListener.onCompensationsPinned(false);
        }
    }

    @Override
    void onCompensationsPaidPinned(String groupId) {
        super.onCompensationsPaidPinned(groupId);

        checkQueryCount();

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onCompensationsPinned(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onCompensationsPinned(boolean isPaid);

        void onCompensationsPinFailed(ParseException e);

        void onAllQueriesFinished();
    }
}
