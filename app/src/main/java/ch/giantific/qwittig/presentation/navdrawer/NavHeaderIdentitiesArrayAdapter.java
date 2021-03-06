/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;


/**
 * Handles the display of a user's groups in the navigation drawer.
 * <p/>
 * Subclass of {@link ArrayAdapter}.
 */
public class NavHeaderIdentitiesArrayAdapter extends ArrayAdapter<Identity> {

    private static final int VIEW_RESOURCE = R.layout.spinner_item_nav;
    private static final int VIEW_RESOURCE_DROPDOWN = android.R.layout.simple_spinner_dropdown_item;

    /**
     * Constructs a new {@link NavHeaderIdentitiesArrayAdapter}.
     *
     * @param context    the context to use in the adapter
     * @param identities the list of identities
     */
    public NavHeaderIdentitiesArrayAdapter(@NonNull Context context,
                                           @NonNull List<Identity> identities) {
        super(context, VIEW_RESOURCE, identities);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent,
                               boolean isDropDown) {
        final GroupRow groupRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(isDropDown
                                                                           ? VIEW_RESOURCE_DROPDOWN
                                                                           : VIEW_RESOURCE, parent, false);
            groupRow = new GroupRow(convertView);

            convertView.setTag(groupRow);
        } else {
            groupRow = (GroupRow) convertView.getTag();
        }

        final Identity identity = getItem(position);
        if (identity != null) {
            groupRow.setGroup(identity.getGroupName());
        }

        return convertView;
    }

    /**
     * Provides an adapter row that displays a user's group
     */
    private static class GroupRow {

        private final TextView mTextViewGroup;

        /**
         * Constructs a new {@link GroupRow}.
         *
         * @param view the inflated view
         */
        GroupRow(@NonNull View view) {
            mTextViewGroup = (TextView) view.findViewById(android.R.id.text1);
        }

        /**
         * Sets the name of the group.
         *
         * @param group the name of the group to set
         */
        public void setGroup(@NonNull String group) {
            mTextViewGroup.setText(group);
        }
    }
}
