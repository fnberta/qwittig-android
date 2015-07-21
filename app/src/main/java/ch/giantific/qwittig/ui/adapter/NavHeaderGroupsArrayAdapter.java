package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.data.parse.models.Group;


/**
 * Created by fabio on 12.10.14.
 */
public class NavHeaderGroupsArrayAdapter extends ArrayAdapter<ParseObject> {

    private static final String LOG_TAG = NavHeaderGroupsArrayAdapter.class.getSimpleName();

    private int mViewResource;
    private int mDropDownViewResource;
    private List<ParseObject> mGroups;
    private Context mContext;

    public NavHeaderGroupsArrayAdapter(Context context, int viewResource, int dropDownViewResource,
                                       List<ParseObject> groups) {
        super(context, viewResource, groups);

        mContext = context;
        mViewResource = viewResource;
        mDropDownViewResource = dropDownViewResource;
        mGroups = groups;
        setDropDownViewResource(dropDownViewResource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent,
                               boolean isDropDown) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            int viewResource;
            if (isDropDown) {
                viewResource = mDropDownViewResource;
            } else {
                viewResource = mViewResource;
            }
            convertView = LayoutInflater.from(parent.getContext()).inflate(viewResource, parent,
                    false);
            viewHolder.mTextViewGroup = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Group group = (Group) mGroups.get(position);
        viewHolder.mTextViewGroup.setText(group.getName());

        return convertView;
    }

    private static class ViewHolder {

        private TextView mTextViewGroup;
    }
}
