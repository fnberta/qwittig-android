package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseConfig;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;

/**
 * Created by fabio on 15.03.15.
 */
public class StoresAdapter extends BaseAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STORE_ADDED = 1;
    private static final int TYPE_STORE_DEFAULT = 2;
    private static final int ITEM_VIEW_TYPE_COUNT = 3;
    private static final int NUMBER_OF_HEADER_VIEWS = 2;
    private Context mContext;
    private int mViewResource;
    private List<String> mStoresDefault;
    private List<String> mStoresAdded;
    private List<String> mStoresFavorites;

    public StoresAdapter(Context context, int viewResource, List<String> storesAdded,
                         List<String> storesFavorites) {
        mContext = context;
        mViewResource = viewResource;
        mStoresAdded = storesAdded;
        mStoresFavorites = storesFavorites;
        mStoresDefault = getDefaultStores();
    }

    private List<String> getDefaultStores() {
        ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(Config.DEFAULT_STORES);
    }

    @Override
    public int getCount() {
        return mStoresDefault.size() + mStoresAdded.size() + NUMBER_OF_HEADER_VIEWS;
    }

    @Override
    public Object getItem(int position) {
        int storesDefaultSize = mStoresDefault.size();
        if (position > 0 && position <= storesDefaultSize) {
            return mStoresDefault.get(position - 1);
        }
        if (position > storesDefaultSize) {
            return mStoresAdded.get(position - storesDefaultSize - NUMBER_OF_HEADER_VIEWS);
        }

        return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_STORE_ADDED;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getAdjustedStoreAddedPosition(int position) {
        return position + mStoresDefault.size() + NUMBER_OF_HEADER_VIEWS;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == mStoresDefault.size() + 1) {
            return TYPE_HEADER;
        }

        if (position > 0 && position <= mStoresDefault.size()) {
            return TYPE_STORE_DEFAULT;
        }

        return TYPE_STORE_ADDED;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final int rowType = getItemViewType(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            if (rowType == TYPE_HEADER) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.row_header, parent, false);
                viewHolder.tvHeader = (TextView) convertView.findViewById(R.id.tv_header);
            } else {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        mViewResource, parent, false);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_store_name);
                viewHolder.cbFavorite = (CheckBox) convertView.findViewById(R.id.cb_store_favorite);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (rowType == TYPE_HEADER) {
            String header = "";
            if (position == 0) {
                header = mContext.getString(R.string.header_stores_default);
            } else {
                header = mContext.getString(R.string.header_stores_own);
            }
            viewHolder.tvHeader.setText(header);
        } else {
            viewHolder.cbFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            String store = (String) getItem(position);
            viewHolder.tvName.setText(store);

            if (mStoresFavorites.contains(store)) {
                viewHolder.cbFavorite.setChecked(true);
            } else {
                viewHolder.cbFavorite.setChecked(false);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView tvHeader;
        private TextView tvName;
        private CheckBox cbFavorite;
    }
}
