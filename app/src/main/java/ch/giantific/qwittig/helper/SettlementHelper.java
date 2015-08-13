package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Created by fabio on 10.12.14.
 */
public class SettlementHelper extends BaseHelper {

    public static final String SETTLEMENT_HELPER = "settlement_helper";
    private static final String SINGLE_USER_SETTLEMENT = "single_user_settlement";
    private static final String LOG_TAG = SettlementHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public SettlementHelper() {
        // empty default constructor
    }

    public static SettlementHelper newInstance(boolean doSingleUserSettlement) {
        SettlementHelper fragment = new SettlementHelper();
        Bundle args = new Bundle();
        args.putBoolean(SINGLE_USER_SETTLEMENT, doSingleUserSettlement);
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

        boolean doSingleUserSettlement = false;
        Bundle args = getArguments();
        if (args != null) {
            doSingleUserSettlement = args.getBoolean(SINGLE_USER_SETTLEMENT, false);
        }

        Group currentGroup = ParseUtils.getCurrentGroup();
        if (currentGroup != null) {
            startNewSettlement(currentGroup, doSingleUserSettlement);
        }
    }

    private void startNewSettlement(ParseObject groupToBalance, boolean doSingleUserSettlement) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupToBalance.getObjectId());
        params.put(CloudCode.PARAM_SETTLEMENT_SINGLE_USER, doSingleUserSettlement);
        ParseCloud.callFunctionInBackground(CloudCode.SETTLEMENT_NEW, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onNewSettlementCreationFailed(e);
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onNewSettlementCreated(o);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onNewSettlementCreated(Object result);

        void onNewSettlementCreationFailed(ParseException e);
    }
}
