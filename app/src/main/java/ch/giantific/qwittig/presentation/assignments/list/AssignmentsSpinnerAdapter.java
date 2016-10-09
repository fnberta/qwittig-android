package ch.giantific.qwittig.presentation.assignments.list;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;

/**
 * Created by fabio on 09.10.16.
 */

public class AssignmentsSpinnerAdapter extends ArrayAdapter<AssignmentDeadline> {

    @NonNull
    private final Context context;
    @LayoutRes
    private final int viewResource;
    @NonNull
    private final AssignmentDeadline[] deadlines;

    public AssignmentsSpinnerAdapter(@NonNull Context context,
                                     @LayoutRes int viewResource,
                                     @NonNull AssignmentDeadline[] deadlines) {
        super(context, viewResource, deadlines);

        this.context = context;
        this.viewResource = viewResource;
        this.deadlines = deadlines;
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

    @NonNull
    private View getCustomView(int position,
                               View convertView,
                               @NonNull ViewGroup parent,
                               boolean isDropDown) {
        final TextView textView;
        if (convertView == null) {
            final int resource = isDropDown
                                 ? android.R.layout.simple_spinner_dropdown_item
                                 : viewResource;
            textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        } else {
            textView = (TextView) convertView;
        }

        final String title = context.getString(deadlines[position].getTitle());
        textView.setText(title);

        return textView;
    }
}
