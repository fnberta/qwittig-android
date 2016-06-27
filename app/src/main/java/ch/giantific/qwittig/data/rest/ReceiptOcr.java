/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Defines the call to the server to perform OCR on an image.
 */
public interface ReceiptOcr {
    /**
     * Makes a POST call to the server to perform the OCR.
     *
     * @param sessionToken the token of the current to authenticate with the server
     * @param receipt      the receipt image to perform OCR on
     * @return the result as an {@link Observable}
     */
    @Multipart
    @POST("receipt")
    Observable<Void> uploadReceipt(@Part("sessionToken") RequestBody sessionToken,
                                   @Part MultipartBody.Part receipt);
}