/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should either pay a compensation or
 * that he should accept an already paid one.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class CompensationRemindHelper extends BaseHelper {

    @IntDef({TYPE_REMIND, TYPE_REMIND_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemindType {}
    public static final int TYPE_REMIND = 1;
    public static final int TYPE_REMIND_PAID = 2;
    private static final String BUNDLE_REMIND_TYPE = "BUNDLE_REMIND_TYPE";
    private static final String BUNDLE_COMPENSATION_ID = "BUNDLE_COMPENSATION_ID";
    private static final String LOG_TAG = CompensationRemindHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    private int mRemindType;
    public CompensationRemindHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CompensationRemindHelper} with the reminder type and
     * the compensation object id as arguments.
     *
     * @param remindType     the type of reminder to send, either to pay a compensation or to
     *                       accept an already paid one
     * @param compensationId the object id of the compensation
     * @return a new instance of {@link CompensationRemindHelper}
     */
    @NonNull
    public static CompensationRemindHelper newInstance(@RemindType int remindType,
                                                       @NonNull String compensationId) {
        CompensationRemindHelper fragment = new CompensationRemindHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_REMIND_TYPE, remindType);
        args.putString(BUNDLE_COMPENSATION_ID, compensationId);
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

        String compensationId = "";
        Bundle args = getArguments();
        if (args != null) {
            mRemindType = args.getInt(BUNDLE_REMIND_TYPE);
            compensationId = args.getString(BUNDLE_COMPENSATION_ID);
        }

        if (TextUtils.isEmpty(compensationId)) {
            return;
        }

        String currencyCode = getCurrencyCode();
        switch (mRemindType) {
            case TYPE_REMIND:
                pushCompensationRemind(compensationId, currencyCode);
                break;
            case TYPE_REMIND_PAID:
                pushCompensationRemindPaid(compensationId, currencyCode);
                break;
        }
    }

    private String getCurrencyCode() {
        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();
        return currentGroup.getCurrency();
    }

    private void pushCompensationRemind(@NonNull final String compensationId,
                                        @NonNull String currencyCode) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_COMPENSATION_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e, compensationId);
                    return;
                }

                if (mListener != null) {
                    mListener.onUserReminded(mRemindType, compensationId);
                }
            }
        });
    }

    private void pushCompensationRemindPaid(@NonNull final String compensationId,
                                            @NonNull String currencyCode) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_COMPENSATION_REMIND_PAID, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e, compensationId);
                    return;
                }

                if (mListener != null) {
                    mListener.onUserReminded(mRemindType, compensationId);
                }
            }
        });
    }

    @NonNull
    private Map<String, Object> getCompensationPushParams(@NonNull String compensationId,
                                                          @NonNull String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }

    private void onParseError(ParseException e, String compensationId) {
        if (mListener != null) {
            mListener.onUserRemindFailed(mRemindType, compensationId, e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the action to take after a user was reminded or the action failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the case when the user was successfully reminded.
         *
         * @param remindType     the type of reminder sent, either to pay a compensation or to
         *                       accept an already paid one
         * @param compensationId the object id of the compensation
         */
        void onUserReminded(int remindType, @NonNull String compensationId);

        /**
         * Handles the case when the attempt to remind the user failed.
         *
         * @param remindType     the type of reminder sent, either to pay a compensation or to
         *                       accept an already paid one
         * @param compensationId the object id of the compensation
         * @param e              the {@link ParseException} thrown during the process
         */
        void onUserRemindFailed(int remindType, @NonNull String compensationId, @NonNull ParseException e);
    }
}
