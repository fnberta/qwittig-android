/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.UserAvatarRowBaseViewModel;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskAddEditUserViewModel extends UserAvatarRowBaseViewModel {

    private float mIdentityAlpha;
    private boolean mNicknameBold;

    public TaskAddEditUserViewModel(@NonNull Identity identity, float identityAlpha,
                                    boolean nicknameBold) {
        super(identity);

        setIdentity(identityAlpha, nicknameBold);
    }

    private void setIdentity(float identityAlpha, boolean nicknameBold) {
        mIdentityAlpha = identityAlpha;
        mNicknameBold = nicknameBold;
    }

    public void updateIdentity(@NonNull Identity identity, float identityAlpha,
                               boolean nicknameBold) {
        super.updateIdentity(identity);

        setIdentity(identityAlpha, nicknameBold);
        notifyChange();
    }

    @Bindable
    public boolean isNicknameBold() {
        return mNicknameBold;
    }

    @Bindable
    public float getIdentityAlpha() {
        return mIdentityAlpha;
    }
}
