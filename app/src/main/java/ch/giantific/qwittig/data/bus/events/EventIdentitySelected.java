package ch.giantific.qwittig.data.bus.events;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 15.06.16.
 */
public class EventIdentitySelected {

    private final Identity mIdentity;

    public EventIdentitySelected(@NonNull Identity identity) {

        mIdentity = identity;
    }

    public Identity getIdentity() {
        return mIdentity;
    }
}
