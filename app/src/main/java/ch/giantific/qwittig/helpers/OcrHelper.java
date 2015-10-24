package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseSession;

import java.io.File;

import ch.giantific.qwittig.data.ocr.RestClient;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by fabio on 10.12.14.
 */
public class OcrHelper extends BaseHelper {

    private static final String LOG_TAG = OcrHelper.class.getSimpleName();
    private static final String BUNDLE_RECEIPT_PATH = "BUNDLE_RECEIPT_PATH";
    private static final int MAX_RETRIES = 0;
    private HelperInteractionListener mListener;
    private String mReceiptPath;
    private int mRetries;

    public OcrHelper() {
        // empty default constructor
    }

    public static OcrHelper newInstance(String receiptPath) {
        OcrHelper fragment = new OcrHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_RECEIPT_PATH, receiptPath);
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

        Bundle args = getArguments();
        if (args != null) {
            mReceiptPath = args.getString(BUNDLE_RECEIPT_PATH);
            if (TextUtils.isEmpty(mReceiptPath)) {
                return;
            }
        }

        getSessionToken();
    }

    private void getSessionToken() {
        ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(ParseSession parseSession, ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        ParseErrorHandler.handleParseError(getActivity(), e);
                        mListener.onOcrFailed(ParseErrorHandler.getErrorMessage(getActivity(), e));
                    }

                    return;
                }

                String sessionToken = parseSession.getSessionToken();
                doReceiptOcr(sessionToken);
            }
        });
    }

    private void doReceiptOcr(String sessionToken) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
        RestClient.getService().uploadReceipt(new TypedString(sessionToken),
                new TypedFile(mimeType, new File(mReceiptPath)),
                new Callback<PurchaseRest>() {
                    @Override
                    public void success(PurchaseRest purchaseRest, Response response) {
                        mRetries = 0;
                        if (mListener != null) {
                            mListener.onOcrFinished(purchaseRest);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(LOG_TAG, "retrofit error " + error.toString() + " retry " + mRetries);

                        if (mRetries < MAX_RETRIES) {
                            getSessionToken();
                            mRetries++;
                        } else if (mListener != null) {
                            mListener.onOcrFailed(error.getLocalizedMessage());
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
        void onOcrFinished(PurchaseRest purchaseRest);

        void onOcrFailed(String errorMessage);
    }
}
