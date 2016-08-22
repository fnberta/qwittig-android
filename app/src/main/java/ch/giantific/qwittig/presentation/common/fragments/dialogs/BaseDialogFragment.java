/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments.dialogs;

import android.content.Context;
import android.support.v4.app.DialogFragment;

/**
 * Provides an abstract base class for dialog fragments.
 */
public abstract class BaseDialogFragment<T> extends DialogFragment {

    protected T activity;

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (T) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DialogInteractionListener");
        }
    }
}
