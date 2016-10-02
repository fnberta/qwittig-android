/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import java.util.Calendar;

import ch.giantific.qwittig.R;

/**
 * Provides an Android standard date picker dialog.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class DatePickerDialogFragment extends BaseDialogFragment<DatePickerDialog.OnDateSetListener> {

    private static final String DIALOG_TAG = DatePickerDialogFragment.class.getCanonicalName();

    /**
     * Displays a new instance of a {@link DatePickerDialogFragment}.
     *
     * @param fm the fragment manager to use for the transaction
     */
    public static void display(@NonNull FragmentManager fm) {
        final DatePickerDialogFragment dialog = new DatePickerDialogFragment();
        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert_PrimaryAsAccent, activity, year, month, day);
    }
}
