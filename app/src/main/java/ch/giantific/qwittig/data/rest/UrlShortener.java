package ch.giantific.qwittig.data.rest;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Single;

/**
 * Defines the call to the google api that shortens url.
 */

public interface UrlShortener {

    /**
     * Makes a POST call to a google server to shorten an url
     *
     * @param apiKey  the api key to use for the request
     * @param request the request containing url to shorten
     * @return the result as an {@link Single}
     */
    @POST("url")
    Single<UrlShortenerResult> shortenUrl(@Query("key") String apiKey,
                                          @Body UrlShortenerRequest request);
}
