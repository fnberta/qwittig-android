package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fabio on 08.08.16.
 */
public class UserIdToken {

    @SerializedName("idToken")
    private final String mIdToken;

    public UserIdToken(@NonNull String idToken) {
        mIdToken = idToken;
    }

    public String getIdToken() {
        return mIdToken;
    }
}
