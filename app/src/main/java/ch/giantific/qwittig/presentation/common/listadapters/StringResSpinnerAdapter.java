/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.listadapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Handles the display of android string resources.
 * <p/>
 * Subclass of {@link BaseAdapter}.
 * <p/>
 * Implements {@link ThemedSpinnerAdapter} in order to show a themed dropdown menu.
 */
public class StringResSpinnerAdapter extends BaseAdapter implements ThemedSpinnerAdapter {

    @NonNull
    private final ThemedSpinnerAdapter.Helper dropDownHelper;
    private final Context context;
    private final int viewResource;
    private final int[] stringRes;

    /**
     * Constructs a new {@link StringResSpinnerAdapter}.
     *
     * @param context      the context to use in the adapter
     * @param viewResource the view viewResource layout to use to display the items
     * @param stringRes    the string resources to display
     */
    public StringResSpinnerAdapter(@NonNull Context context, int viewResource,
                                   @NonNull int[] stringRes) {
        super();

        this.dropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        this.context = context;
        this.viewResource = viewResource;
        this.stringRes = stringRes;
    }

    @Override
    public int getCount() {
        return stringRes.length;
    }

    @Override
    public Object getItem(int position) {
        return stringRes[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Nullable
    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent,
                               boolean isDropDown) {
        final TypeRow typeRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    isDropDown ? android.R.layout.simple_spinner_dropdown_item : viewResource,
                    parent, false);
            typeRow = new TypeRow(convertView);
            convertView.setTag(typeRow);
        } else {
            typeRow = (TypeRow) convertView.getTag();
        }

        String title = context.getString(stringRes[position]);
        typeRow.setTitle(title);

        return convertView;
    }

    @Nullable
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    /**
     * Returns the adapter position of the passed in string resources or -1 if it does not exist
     * in the adapter
     *
     * @param stringRes the string resource the position should be returned for
     * @return the adapter position of the string resources or -1 if it does not exist
     */
    public int getPosition(int stringRes) {
        for (int i = 0, stringResLength = this.stringRes.length; i < stringResLength; i++) {
            if (this.stringRes[i] == stringRes) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return dropDownHelper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        dropDownHelper.setDropDownViewTheme(theme);
    }

    /**
     * Provides an adapter row that displays a resolved android string resource as a simple title.
     * <p/>
     * Expects the title {@link TextView} to use the standard <code>android.R.id.text1</code> as
     * its id.
     */
    private static class TypeRow {

        private final TextView tvTitle;

        /**
         * Constructs a new {@link TypeRow}.
         *
         * @param view the inflated view
         */
        public TypeRow(@NonNull View view) {
            tvTitle = (TextView) view.findViewById(android.R.id.text1);
        }

        /**
         * Sets the title text.
         *
         * @param title the title text to set
         */
        public void setTitle(@NonNull String title) {
            tvTitle.setText(title);
        }
    }
}
