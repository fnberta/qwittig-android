/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 16.01.16.
 */
public class TaskUserInvolvedRowViewModel extends UserAvatarRowBaseViewModel {

    private float mUserAlpha;
    private boolean mUserNameBold;

    public TaskUserInvolvedRowViewModel(@NonNull User user, float userAlpha, boolean userNameBold) {
        super(user);

        mUserAlpha = userAlpha;
        mUserNameBold = userNameBold;
    }

    @Bindable
    public boolean isUserNameBold() {
        return mUserNameBold;
    }

    public void setUserNameBold(boolean userNameBold) {
        mUserNameBold = userNameBold;
        notifyPropertyChanged(BR.userNameBold);
    }

    @Bindable
    public float getUserAlpha() {
        return mUserAlpha;
    }

    public void setUserAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        mUserAlpha = alpha;
        notifyPropertyChanged(BR.userAlpha);
    }
}
