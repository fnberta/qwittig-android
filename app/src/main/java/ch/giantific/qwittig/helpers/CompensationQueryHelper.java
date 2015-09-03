package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by fabio on 10.12.14.
 */
public class CompensationQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = CompensationQueryHelper.class.getSimpleName();
    private static final String QUERY_PAID = "query_paid";
    private boolean mQueryPaid;
    private HelperInteractionListener mListener;

    public CompensationQueryHelper() {
        // empty default constructor
    }

    public static CompensationQueryHelper newInstance(boolean queryPaid) {
        CompensationQueryHelper fragment = new CompensationQueryHelper();
        Bundle args = new Bundle();
        args.putBoolean(QUERY_PAID, queryPaid);
        fragment.setArguments(args);
        return fragment;
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

        Bundle args = getArguments();
        if (args != null) {
            mQueryPaid = args.getBoolean(QUERY_PAID, false);
        }

        if (mCurrentGroup == null || mCurrentUserGroups == null) {
            mListener.onAllQueriesFinished(mQueryPaid);
            return;
        }

        if (mQueryPaid) {
            mTotalNumberOfQueries = mCurrentUserGroups.size();
            for (ParseObject group : mCurrentUserGroups) {
                queryCompensationsPaid(group);
            }
        } else {
            mTotalNumberOfQueries = 1;
            queryCompensationsUnpaid(mCurrentUserGroups);
        }
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onCompensationsPinFailed(e, mQueryPaid);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllQueriesFinished(mQueryPaid);
        }
    }

    @Override
    void onCompensationsUnpaidPinned() {
        super.onCompensationsUnpaidPinned();

        if (mListener != null) {
            mListener.onCompensationsPinned(false);
        }

        checkQueryCount();
    }

    @Override
    void onCompensationsPaidPinned(String groupId) {
        super.onCompensationsPaidPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onCompensationsPinned(true);
        }

        checkQueryCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onCompensationsPinned(boolean isPaid);

        void onCompensationsPinFailed(ParseException e, boolean isPaid);

        void onAllQueriesFinished(boolean isPaid);
    }
}
