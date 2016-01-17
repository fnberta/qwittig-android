/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Provides a client to connect to the REST api of a specified server to perform the OCR on an
 * image.
 */
public class OcrClient {

    private static final String BASE_URL = "http://37.120.165.175:3000";
    private static final long READ_TIMEOUT = 300000;
    private static final Retrofit REST_ADAPTER = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
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

}
