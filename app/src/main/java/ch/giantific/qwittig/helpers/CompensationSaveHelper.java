package ch.giantific.qwittig.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import ch.giantific.qwittig.data.parse.models.Compensation;

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
