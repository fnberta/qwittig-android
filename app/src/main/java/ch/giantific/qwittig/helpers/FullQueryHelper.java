package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;

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
            mListener.onFullQueryFinished();
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
            mListener.onFullQueryFinished();
        }
    }

    @Override
    void onUsersPinned() {
        super.onUsersPinned();

        if (mListener != null) {
            mListener.onUsersPinned();
        }

        if (!checkQueryCount()) {
            queryPurchases();
        }
    }

    @Override
    void onPurchasesPinned(String groupId) {
        super.onPurchasesPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onPurchasesPinned();
        }

        if (!checkQueryCount()) {
            queryCompensations();
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
        void onUsersPinned();

        void onPurchasesPinned();

        void onCompensationsPinned(boolean isPaid);

        void onFullQueryFinished();

        void onPinFailed(ParseException e);
    }
}
