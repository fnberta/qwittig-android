package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;

import ch.giantific.qwittig.data.ocr.RestClient;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by fabio on 10.12.14.
 */
public class OcrHelper extends Fragment {

    private static final String BUNDLE_RECEIPT = "receipt";
    private HelperInteractionListener mListener;

    public OcrHelper() {
        // empty default constructor
    }

    public static OcrHelper newInstance(String receiptPath) {
        OcrHelper fragment = new OcrHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_RECEIPT, receiptPath);
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

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        String receiptPath = "";
        Bundle args = getArguments();
        if (args != null) {
            receiptPath = args.getString(BUNDLE_RECEIPT);
        }
        
        if (!TextUtils.isEmpty(receiptPath)) {
            doReceiptOcr(receiptPath);
        }
    }

    private void doReceiptOcr(String receiptPath) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
        RestClient.getService().uploadReceipt(new TypedFile(mimeType, new File(receiptPath)),
                new Callback<PurchaseRest>() {
                    @Override
                    public void success(PurchaseRest purchaseRest, Response response) {
                        if (mListener != null) {
                            mListener.onOcrSuccessful(purchaseRest);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mListener != null) {
                            mListener.onOcrFailed(error);
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
        void onOcrSuccessful(PurchaseRest purchaseRest);
        void onOcrFailed(RetrofitError error);
    }
}
