package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by fabio on 10.07.16.
 */
public interface FirebaseModel {

    String PATH_CREATED_AT = "createdAt";

    String getId();

    void setId(@NonNull String id);

    Map<String, String> getCreatedAt();
}
