/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseSession;

import java.io.File;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import ch.giantific.qwittig.data.rest.OcrClient;
import ch.giantific.qwittig.ParseErrorHandler;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Sends the image of receipt to the server to analyse and ocr it using
 * {@link OcrClient.ReceiptOcr}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class OcrWorker extends BaseWorker {

    private static final String LOG_TAG = OcrWorker.class.getSimpleName();
    private static final String BUNDLE_RECEIPT_PATH = "BUNDLE_RECEIPT_PATH";
    private static final int MAX_RETRIES = 0;
    @Nullable
    private WorkerInteractionListener mListener;
    private String mReceiptPath;
    private int mRetries;

    public OcrWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link OcrWorker} with the path to a receipt image as an argument.
     *
     * @param receiptPath the path to the image of the receipt to perform ocr on
     * @return a new instance of {@link OcrWorker}
     */
    @NonNull
    public static OcrWorker newInstance(@NonNull String receiptPath) {
        OcrWorker fragment = new OcrWorker();
        Bundle args = new Bundle();
        args.putString(BUNDLE_RECEIPT_PATH, receiptPath);
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
            mReceiptPath = args.getString(BUNDLE_RECEIPT_PATH, "");
        }

        if (TextUtils.isEmpty(mReceiptPath)) {
            if (mListener != null) {
                mListener.onOcrFailed(R.string.toast_unknown_error);
            }

            return;
        }

        getSessionToken();
    }

    private void getSessionToken() {
        ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(@NonNull ParseSession parseSession, @Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onOcrFailed(ParseErrorHandler.handleParseError(getActivity(), e));
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
        OcrClient.getService().uploadReceipt(new TypedString(sessionToken),
                new TypedFile(mimeType, new File(mReceiptPath)),
                new Callback<OcrPurchase>() {
                    @Override
                    public void success(OcrPurchase ocrPurchase, Response response) {
                        mRetries = 0;
                        if (mListener != null) {
                            mListener.onOcrFinished(ocrPurchase);
                        }
                    }

                    @Override
                    public void failure(@NonNull RetrofitError error) {
                        if (mRetries < MAX_RETRIES) {
                            getSessionToken();
                            mRetries++;
                        } else if (mListener != null) {
                            mListener.onOcrFailed(R.string.toast_unknown_error);
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
    public interface WorkerInteractionListener {
        /**
         * Handles the successful ocr analysis of an image.
         *
         * @param ocrPurchase the parsed data from the ocr analysis
         */
        void onOcrFinished(@NonNull OcrPurchase ocrPurchase);

        /**
         * Handles the failed ocr analysis of an image.
         *
         * @param errorMessage the error message received from the server
         */
        void onOcrFailed(@StringRes int errorMessage);
    }
}
