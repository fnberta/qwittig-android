package ch.giantific.qwittig.helper;

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

        runFullQuery();
    }

    private void runFullQuery() {
        if (mCurrentGroup != null) {
            calculateBalance();
        } else {
            queryUsers();
        }
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
    void onUsersPinned() {
        super.onUsersPinned();

        if (mListener != null) {
            mListener.onUsersPinned();
        }

        queryPurchases();
    }

    @Override
    void onPurchasesPinned(String groupId) {
        super.onPurchasesPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onPurchasesPinned();
        }

        queryCompensations();
    }

    @Override
    void onCompensationsUnpaidPinned() {
        super.onCompensationsUnpaidPinned();

        if (mListener != null) {
            mListener.onCompensationsPinned(false);
        }
    }

    @Override
    void onCompensationsPaidPinned(String groupId) {
        super.onCompensationsPaidPinned(groupId);

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

        void onPinFailed(ParseException e);
    }
}
