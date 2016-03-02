/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Provides a themed array adapter that displays objects.
 * <p/>
 * Subclass of {@link ArrayAdapter}.
 * <p/>
 * Implements {@link ThemedSpinnerAdapter} in order to provide a themed dropdown menu.
 */
public class ThemedArrayAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {

    @NonNull
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;

    public ThemedArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects) {
        super(context, resource, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(@NonNull Context context, int resource, int textViewResourceId,
                              @NonNull T[] objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(@NonNull Context context, int resource, int textViewResourceId,
                              @NonNull List<T> objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }
}
