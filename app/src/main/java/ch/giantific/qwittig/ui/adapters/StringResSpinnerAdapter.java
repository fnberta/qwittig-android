package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;

import ch.giantific.qwittig.data.parse.models.Task;


/**
 * Created by fabio on 28.08.15.
 */
public class StringResSpinnerAdapter extends BaseAdapter implements ThemedSpinnerAdapter {

    private final ThemedSpinnerAdapter.Helper mDropDownHelper;
    private Context mContext;
    private int mViewResource;
    private int[] mStringRes;

    public StringResSpinnerAdapter(Context context, int resource, int[] stringRes) {
        super();

        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        mContext = context;
        mViewResource = resource;
        mStringRes = stringRes;
    }

    @Override
    public int getCount() {
        return mStringRes.length;
    }

    @Override
    public Object getItem(int position) {
        return mStringRes[position];
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

        String title = mContext.getString(mStringRes[position]);
        typeRow.setTitle(title);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    public int getPosition(int stringRes) {
        for (int i = 0, stringResLength = mStringRes.length; i < stringResLength; i++) {
            if (mStringRes[i] == stringRes) {
                return i;
            }
        }

        return -1;
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
