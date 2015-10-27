/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
 * Sends the image of receipt to the server to analyse and ocr it using
 * {@link RestClient.ReceiptOcr}.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class OcrHelper extends BaseHelper {

    private static final String LOG_TAG = OcrHelper.class.getSimpleName();
    private static final String BUNDLE_RECEIPT_PATH = "BUNDLE_RECEIPT_PATH";
    private static final int MAX_RETRIES = 0;
    @Nullable
    private HelperInteractionListener mListener;
    private String mReceiptPath;
    private int mRetries;

    public OcrHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link OcrHelper} with the path to a receipt image as an argument.
     *
     * @param receiptPath the path to the image of the receipt to perform ocr on
     * @return a new instance of {@link OcrHelper}
     */
    @NonNull
    public static OcrHelper newInstance(@NonNull String receiptPath) {
        OcrHelper fragment = new OcrHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_RECEIPT_PATH, receiptPath);
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

        Bundle args = getArguments();
        if (args != null) {
            mReceiptPath = args.getString(BUNDLE_RECEIPT_PATH, "");
        }

        if (!TextUtils.isEmpty(mReceiptPath)) {
            getSessionToken();
        }
    }

    private void getSessionToken() {
        ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(@NonNull ParseSession parseSession, @Nullable ParseException e) {
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

    private void doReceiptOcr(@NonNull String sessionToken) {
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
                    public void failure(@NonNull RetrofitError error) {
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

    /**
     * Defines the action to take after the image has been ocr or after the process failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful ocr analysis of an image.
         *
         * @param purchaseRest the parsed data from the ocr analysis
         */
        void onOcrFinished(@NonNull PurchaseRest purchaseRest);

        /**
         * Handles the failed ocr analysis of an image.
         *
         * @param errorMessage the error message received from the server
         */
        void onOcrFailed(@NonNull String errorMessage);
    }
}
