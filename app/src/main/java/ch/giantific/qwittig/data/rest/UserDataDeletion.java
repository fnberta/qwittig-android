/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Single;

/**
 * Defines the call to the server to perform OCR on an image.
 */
public interface UserDataDeletion {
    /**
     * Makes a POST call to the server to perform the OCR.
     *
     * @param userIdToken the token of the current to authenticate with the server
     * @return the result as an {@link Single}
     */
    @POST("user/delete")
    Single<Void> deleteUserData(@Body UserIdToken userIdToken);
}
