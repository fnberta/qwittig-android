package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;

/**
 * Created by fabio on 10.12.14.
 */
public class FullQueryHelper extends BaseQueryHelper {

    public static final String FULL_QUERY_HELPER = "full_query_helper";
    private static final String LOG_TAG = FullQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public FullQueryHelper() {
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

        // purchases for each group + compensationsPaid for each group + compensationsUnpaid + users
        mTotalNumberOfQueries = mCurrentUserGroups.size() * 2 + 1 + 1;

        calculateBalance();
    }

    private void calculateBalance() {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CloudCode.CALCULATE_BALANCE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                queryUsers();
            }
        });
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllQueriesFinished();
        }
    }

    @Override
    void onUsersPinned() {
        super.onUsersPinned();

        checkQueryCount();

        if (mListener != null) {
            mListener.onUsersPinned();
        }

        queryPurchases();
    }

    @Override
    void onPurchasesPinned(String groupId) {
        super.onPurchasesPinned(groupId);

        checkQueryCount();

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onPurchasesPinned();
        }

        queryCompensations();
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
        void onUsersPinned();

        void onPurchasesPinned();

        void onCompensationsPinned(boolean isPaid);

        void onAllQueriesFinished();

        void onPinFailed(ParseException e);
    }
}
