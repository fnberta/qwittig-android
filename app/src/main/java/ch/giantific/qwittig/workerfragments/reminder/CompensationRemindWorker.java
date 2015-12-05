/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import ch.giantific.qwittig.workerfragments.BaseWorker;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should either pay a compensation or
 * that he should accept an already paid one.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CompensationRemindWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener {

    @IntDef({TYPE_REMIND, TYPE_REMIND_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemindType {}
    public static final int TYPE_REMIND = 1;
    public static final int TYPE_REMIND_PAID = 2;
    private static final String BUNDLE_REMIND_TYPE = "BUNDLE_REMIND_TYPE";
    private static final String BUNDLE_COMPENSATION_ID = "BUNDLE_COMPENSATION_ID";
    private static final String LOG_TAG = CompensationRemindWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;
    private int mRemindType;
    private String mCompensationId;
    public CompensationRemindWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CompensationRemindWorker} with the reminder type and
     * the compensation object id as arguments.
     *
     * @param remindType     the type of reminder to send, either to pay a compensation or to
     *                       accept an already paid one
     * @param compensationId the object id of the compensation
     * @return a new instance of {@link CompensationRemindWorker}
     */
    @NonNull
    public static CompensationRemindWorker newInstance(@RemindType int remindType,
                                                       @NonNull String compensationId) {
        CompensationRemindWorker fragment = new CompensationRemindWorker();
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
            mListener = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mRemindType = args.getInt(BUNDLE_REMIND_TYPE, 0);
            mCompensationId = args.getString(BUNDLE_COMPENSATION_ID, "");
        }

        if (TextUtils.isEmpty(mCompensationId)) {
            if (mListener != null) {
                mListener.onUserRemindFailed(mRemindType, mCompensationId,
                        R.string.toast_unknown_error);
            }

            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient(getActivity());
        String currencyCode = getCurrencyCode();
        switch (mRemindType) {
            case TYPE_REMIND:
                cloudCode.pushCompensationReminder(mCompensationId, currencyCode, this);
                break;
            case TYPE_REMIND_PAID:
                cloudCode.pushCompensationPaidReminder(mCompensationId, currencyCode, this);
                break;
            default:
                if (mListener != null) {
                    mListener.onUserRemindFailed(mRemindType, mCompensationId,
                            R.string.toast_unknown_error);
                }
        }
    }

    private String getCurrencyCode() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group currentGroup = currentUser.getCurrentGroup();
        return ParseUtils.getGroupCurrencyWithFallback(currentGroup);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        if (mListener != null) {
            mListener.onUserReminded(mRemindType, mCompensationId);
        }
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onUserRemindFailed(mRemindType, mCompensationId, errorMessage);
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
    public interface WorkerInteractionListener {
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
         * @param errorMessage   the error message from the exception thrown during the process
         */
        void onUserRemindFailed(int remindType, @NonNull String compensationId,
                                @StringRes int errorMessage);
    }
}
