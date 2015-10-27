/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Creates and saves a new user settlement by calling Parse.com cloud functions.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class SettlementHelper extends BaseHelper {

    private static final String BUNDLE_SINGLE_USER = "BUNDLE_SINGLE_USER";
    private static final String LOG_TAG = SettlementHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public SettlementHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link SettlementHelper}.
     *
     * @param doSingleUserSettlement whether the settlement should be calculated only for the
     *                               current user or for all users of the current group
     * @return a new instance of {@link SettlementHelper}
     */
    @NonNull
    public static SettlementHelper newInstance(boolean doSingleUserSettlement) {
        SettlementHelper fragment = new SettlementHelper();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_SINGLE_USER, doSingleUserSettlement);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean doSingleUserSettlement = false;
        Bundle args = getArguments();
        if (args != null) {
            doSingleUserSettlement = args.getBoolean(BUNDLE_SINGLE_USER, false);
        }

        Group currentGroup = ParseUtils.getCurrentGroup();
        if (currentGroup != null) {
            startNewSettlement(currentGroup, doSingleUserSettlement);
        }
    }

    private void startNewSettlement(@NonNull ParseObject groupToBalance,
                                    boolean doSingleUserSettlement) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupToBalance.getObjectId());
        params.put(CloudCode.PARAM_SETTLEMENT_SINGLE_USER, doSingleUserSettlement);
        ParseCloud.callFunctionInBackground(CloudCode.SETTLEMENT_NEW, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
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

    /**
     * Defines the actions to take after a new settlement was calculated or after the calculation
     * failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful calculation of a new settlement.
         *
         * @param result the result returned from the cloud function
         */
        void onNewSettlementCreated(@NonNull Object result);

        /**
         * Handles the failed calculation of a new settlement.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onNewSettlementCreationFailed(@NonNull ParseException e);
    }
}
