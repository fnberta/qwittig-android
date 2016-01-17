/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import com.squareup.okhttp.RequestBody;

import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
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
     */
    @Multipart
    @POST("/api/receipt")
    Observable<OcrPurchase> uploadReceipt(@Part("sessionToken") RequestBody sessionToken,
                                          @Part("receipt") RequestBody receipt);
}
