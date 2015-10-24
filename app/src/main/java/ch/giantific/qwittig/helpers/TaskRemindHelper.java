package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;

/**
 * Created by fabio on 10.12.14.
 */
public class TaskRemindHelper extends BaseHelper {

    private static final String BUNDLE_TASK_ID = "BUNDLE_TASK_ID";
    private static final String LOG_TAG = TaskRemindHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public TaskRemindHelper() {
        // empty default constructor
    }

    public static TaskRemindHelper newInstance(String taskId) {
        TaskRemindHelper fragment = new TaskRemindHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_TASK_ID, taskId);
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

        String taskId = "";
        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getString(BUNDLE_TASK_ID);
        }

        if (!TextUtils.isEmpty(taskId)) {
            pushTaskRemind(taskId);
        }
    }

    private void pushTaskRemind(final String taskId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK, taskId);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_TASK_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                if (e != null) {
                    onParseError(e, taskId);
                    return;
                }

                if (mListener != null) {
                    mListener.onUserReminded(taskId);
                }
            }
        });
    }

    private void onParseError(ParseException e, String taskId) {
        if (mListener != null) {
            mListener.onUserRemindFailed(e, taskId);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onUserReminded(String taskId);

        void onUserRemindFailed(ParseException e, String taskId);
    }
}
