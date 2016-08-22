package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

/**
 * Created by fabio on 08.08.16.
 */
public class UserIdToken {

    private final String idToken;

    public UserIdToken(@NonNull String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }
}
