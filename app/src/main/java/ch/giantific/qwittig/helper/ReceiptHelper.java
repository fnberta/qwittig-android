package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import ch.giantific.qwittig.data.models.ImageReceipt;

/**
 * Created by fabio on 10.12.14.
 */
public class ReceiptHelper extends Fragment {

    private static final String BUNDLE_PATH = "path";
    private HelperInteractionListener mListener;
    private String mImagePath;

    public static ReceiptHelper newInstance(String imagePath) {
        ReceiptHelper fragment = new ReceiptHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_PATH, imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (getArguments() != null) {
            mImagePath = getArguments().getString(BUNDLE_PATH);
        }

        ReceiptWorkerTask receiptWorkerTask = new ReceiptWorkerTask();
        receiptWorkerTask.execute(mImagePath);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        public void onPostExecute(ImageReceipt receipt);
    }

    private class ReceiptWorkerTask extends AsyncTask<String, Void, ImageReceipt> {

        @Override
        protected ImageReceipt doInBackground(String... params) {
            String imagePath = params[0];
            return new ImageReceipt(getActivity(), imagePath); // TODO: getActivity() could be null
        }

        @Override
        protected void onPostExecute(ImageReceipt receipt) {
            if (mListener != null) {
                mListener.onPostExecute(receipt);
            }
        }
    }
}
