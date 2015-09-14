package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class CompensationRemindHelper extends BaseHelper {

    @IntDef({TYPE_REMIND, TYPE_REMIND_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemindType {}
    public static final int TYPE_REMIND = 1;
    public static final int TYPE_REMIND_PAID = 2;
    private static final String REMIND_TYPE = "remind_type";
    private static final String COMPENSATION_ID = "compensation_id";
    private static final String LOG_TAG = CompensationRemindHelper.class.getSimpleName();
    private HelperInteractionListener mListener;
    private int mRemindType;

    public CompensationRemindHelper() {
        // empty default constructor
    }

    public static CompensationRemindHelper newInstance(@RemindType int remindType, String compensationId) {
        CompensationRemindHelper fragment = new CompensationRemindHelper();
        Bundle args = new Bundle();
        args.putInt(REMIND_TYPE, remindType);
        args.putString(COMPENSATION_ID, compensationId);
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

        String compensationId = "";
        Bundle args = getArguments();
        if (args != null) {
            mRemindType = args.getInt(REMIND_TYPE);
            compensationId = args.getString(COMPENSATION_ID);
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

    private void pushCompensationRemind(final String compensationId, String currencyCode) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_COMPENSATION_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
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

    private void pushCompensationRemindPaid(final String compensationId, String currencyCode) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_COMPENSATION_REMIND_PAID, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
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

    private Map<String, Object> getCompensationPushParams(String compensationId,
                                                                String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }

    private void onParseError(ParseException e, String compensationId) {
        if (mListener != null) {
            mListener.onFailedToRemindUser(mRemindType, e, compensationId);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onUserReminded(int remindType, String compensationId);

        void onFailedToRemindUser(int remindType, ParseException e, String compensationId);
    }
}
