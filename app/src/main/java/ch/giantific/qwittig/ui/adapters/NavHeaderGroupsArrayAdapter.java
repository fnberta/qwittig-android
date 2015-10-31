/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;


/**
 * Handles the display of a user's groups in the navigation drawer.
 * <p/>
 * Subclass of {@link ArrayAdapter}.
 */
public class NavHeaderGroupsArrayAdapter extends ArrayAdapter<ParseObject> {

    private static final String LOG_TAG = NavHeaderGroupsArrayAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.spinner_item_nav;
    private static final int VIEW_RESOURCE_DROPDOWN = android.R.layout.simple_spinner_dropdown_item;
    private List<ParseObject> mGroups;

    /**
     * Constructs a new {@link NavHeaderGroupsArrayAdapter}.
     *
     * @param context the context to use in the adapter
     * @param groups  the groups to display
     */
    public NavHeaderGroupsArrayAdapter(@NonNull Context context,
                                       @NonNull List<ParseObject> groups) {
        super(context, VIEW_RESOURCE, groups);

        mGroups = groups;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Nullable
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @Nullable
    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent,
                               boolean isDropDown) {
        final GroupRow groupRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    isDropDown ? VIEW_RESOURCE_DROPDOWN : VIEW_RESOURCE, parent, false);
            groupRow = new GroupRow(convertView);

            convertView.setTag(groupRow);
        } else {
            groupRow = (GroupRow) convertView.getTag();
        }

        Group group = (Group) mGroups.get(position);
        groupRow.setGroup(group.getName());

        return convertView;
    }

    /**
     * Provides an adapter row that displays a user's group
     */
    private static class GroupRow {

        private TextView mTextViewGroup;

        /**
         * Constructs a new {@link GroupRow}.
         *
         * @param view the inflated view
         */
        public GroupRow(@NonNull View view) {
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
