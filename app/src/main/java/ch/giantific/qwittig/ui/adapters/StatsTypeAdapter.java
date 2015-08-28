package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by fabio on 28.08.15.
 */
public class StatsTypeAdapter extends BaseAdapter {

    private Context mContext;
    private int mViewResource;
    private int[] mStatsTypes;

    public StatsTypeAdapter(Context context, int resource, int[] types) {
        super();

        mContext = context;
        mViewResource = resource;
        mStatsTypes = types;
    }

    @Override
    public int getCount() {
        return mStatsTypes.length;
    }

    @Override
    public Object getItem(int position) {
        return mStatsTypes[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent,
                               boolean isDropDown) {
        final TypeRow typeRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    isDropDown ? android.R.layout.simple_spinner_dropdown_item : mViewResource,
                    parent, false);
            typeRow = new TypeRow(convertView);
            convertView.setTag(typeRow);
        } else {
            typeRow = (TypeRow) convertView.getTag();
        }

        String title = mContext.getString(mStatsTypes[position]);
        typeRow.setTitle(title);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    private static class TypeRow {
        private TextView mTextViewTitle;

        public TypeRow(View view) {
            mTextViewTitle = (TextView) view.findViewById(android.R.id.text1);
        }

        public void setTitle(String title) {
            mTextViewTitle.setText(title);
        }
    }
}
