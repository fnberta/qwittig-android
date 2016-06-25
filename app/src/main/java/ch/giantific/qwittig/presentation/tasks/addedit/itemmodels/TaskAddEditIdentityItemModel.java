/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.BaseIdentityAvatarRowViewModel;

/**
 * Defines a view model for a {@link RecyclerView} row that represents an identity available for
 * selection for a task.
 */
public class TaskAddEditIdentityItemModel extends BaseIdentityAvatarRowViewModel {

    private float mIdentityAlpha;

    public TaskAddEditIdentityItemModel(@NonNull Identity identity, float identityAlpha) {
        super(identity);

        setIdentity(identityAlpha);
    }

    private void setIdentity(float identityAlpha) {
        mIdentityAlpha = identityAlpha;
    }

    public void updateIdentity(@NonNull Identity identity, float identityAlpha) {
        super.updateIdentity(identity);

        setIdentity(identityAlpha);
        notifyChange();
    }

    @Bindable
    public float getIdentityAlpha() {
        return mIdentityAlpha;
    }
}
