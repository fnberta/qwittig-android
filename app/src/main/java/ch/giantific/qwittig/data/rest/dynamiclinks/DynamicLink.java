package ch.giantific.qwittig.data.rest.dynamiclinks;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Single;

/**
 * Defines the call to firebase that generate a short dynamic link.
 */

public interface DynamicLink {

    /**
     * Makes a POST call to firebase to generate a short dynamic link
     *
     * @param apiKey  the api key to use for the request
     * @param request the request containing the parameters for the link
     * @return the result as an {@link Single}
     */
    @POST("shortLinks")
    Single<LinkResult> getDynamicLink(@Query("key") String apiKey,
                                      @Body LinkRequest request);
}
