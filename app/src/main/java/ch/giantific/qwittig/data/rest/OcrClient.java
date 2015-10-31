/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Provides a client to connect to the REST api of a specified server to perform the OCR on an
 * image.
 */
public class OcrClient {

    private static final String BASE_URL = "http://37.120.165.175:3000";
    private static final long READ_TIMEOUT = 300000;
    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(BASE_URL)
            .setClient(new OkClient(generateOkHttp()))
            .build();
    private static final ReceiptOcr RECEIPT_OCR_SERVICE = REST_ADAPTER.create(ReceiptOcr.class);

    private OcrClient() {
        // class cannot be instantiated
    }

    /**
     * Returns the static singleton reference to the {@link ReceiptOcr} instance.
     *
     * @return the static singleton instance of the {@link ReceiptOcr}
     */
    public static ReceiptOcr getService() {
        return RECEIPT_OCR_SERVICE;
    }

    @NonNull
    private static OkHttpClient generateOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        return client;
    }

    /**
     * Defines the call to the server to perform OCR on an image.
     */
    public interface ReceiptOcr {
        /**
         * Makes a POST call to the server to perform the OCR.
         *
         * @param sessionToken the token of the current to authenticate with the server
         * @param receipt      the receipt image to perform OCR on
         * @param callback     the callback that gets called with server's response
         */
        @Multipart
        @POST("/api/receipt")
        void uploadReceipt(@Part("sessionToken") TypedString sessionToken,
                           @Part("receipt") TypedFile receipt,
                           Callback<OcrPurchase> callback);
    }
}
