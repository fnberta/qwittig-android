package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

/**
 * Created by fabio on 02.10.16.
 */

public class UrlShortenerRequest {

    private final String longUrl;

    public UrlShortenerRequest(@NonNull String longUrl) {
        this.longUrl = longUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }
}
