/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Provides an abstract base class for dialog fragments.
 */
public abstract class BaseDialogFragment<T> extends DialogFragment {

    protected T mActivity;

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mActivity = (T) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }
}
