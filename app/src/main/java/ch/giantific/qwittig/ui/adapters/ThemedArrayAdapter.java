package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by fabio on 11.09.15.
 */
public class ThemedArrayAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {

    private final ThemedSpinnerAdapter.Helper mDropDownHelper;

    public ThemedArrayAdapter(Context context, int resource) {
        super(context, resource);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public ThemedArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }
}
