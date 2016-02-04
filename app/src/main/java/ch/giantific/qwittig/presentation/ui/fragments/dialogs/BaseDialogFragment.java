/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.support.annotation.NonNull;

/**
 * Created by fabio on 03.02.16.
 */
public abstract class BaseDialogFragment<T> extends DialogFragment {

    T mActivity;

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
