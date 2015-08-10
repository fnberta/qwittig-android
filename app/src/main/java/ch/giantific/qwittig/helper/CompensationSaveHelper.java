package ch.giantific.qwittig.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;

/**
 * Created by fabio on 10.12.14.
 */
public class CompensationSaveHelper extends BaseHelper {

    private static final String LOG_TAG = CompensationSaveHelper.class.getSimpleName();
    private HelperInteractionListener mListener;
    private Compensation mCompensation;

    public CompensationSaveHelper() {
        // empty default constructor
    }

    /**
     * Using a non empty constructor to be able to pass a ParseObject. Because the fragment will be
     * retained, we don't run the  risk that the system will recreate it with the default empty
     * constructor.
     * @param compensation the ParseObject to save
     */
    @SuppressLint("ValidFragment")
    public CompensationSaveHelper(ParseObject compensation) {
        mCompensation = (Compensation) compensation;
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

        saveCompensation();
    }

    private void saveCompensation() {
        mCompensation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    mCompensation.setPaid(false);

                    if (mListener != null) {
                        mListener.onCompensationSaveFailed(mCompensation, e);
                    }

                    return;
                }

                if (mListener != null) {
                    mListener.onCompensationSaved(mCompensation);
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
        void onCompensationSaved(ParseObject compensation);

        void onCompensationSaveFailed(ParseObject compensation, ParseException e);
    }
}
