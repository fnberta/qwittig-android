/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

import android.support.annotation.StringRes;
import android.view.View;

/**
 * Provides a container for a message action with a text to display and the actions to take.
 */
public abstract class MessageAction implements View.OnClickListener {

    @StringRes
    private int mActionText;

    public MessageAction(int actionText) {
        mActionText = actionText;
    }

    @StringRes
    public int getActionText() {
        return mActionText;
    }

    public void setActionText(@StringRes int actionText) {
        mActionText = actionText;
    }
}
