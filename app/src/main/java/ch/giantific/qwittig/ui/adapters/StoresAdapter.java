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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseConfig;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;

/**
 * Handles the display of the stores the user has saved in his/her profile.
 * <p/>
 * These are the stores the user can choose from when creating/editing a purchase.
 * <p/>
 * Subclass of {@link BaseAdapter}.
 */
public class StoresAdapter extends BaseAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STORE_ADDED = 1;
    private static final int TYPE_STORE_DEFAULT = 2;
    private static final int ITEM_VIEW_TYPE_COUNT = 3;
    private static final int NUMBER_OF_HEADER_VIEWS = 2;
    private static final int VIEW_RESOURCE = R.layout.row_stores;
    private Context mContext;
    private List<String> mStoresDefault;
    private List<String> mStoresAdded;
    private List<String> mStoresFavorites;

    /**
     * Constructs a new {@link StoresAdapter}.
     *
     * @param context         the context to use in the adapter
     * @param storesAdded     the stores the user has added himself
     * @param storesFavorites the stores the user has marked as favorite
     */
    public StoresAdapter(@NonNull Context context, @NonNull List<String> storesAdded,
                         @NonNull List<String> storesFavorites) {
        super();

        mContext = context;
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
    public String getItem(int position) {
        int storesDefaultSize = mStoresDefault.size();
        if (position > 0 && position <= storesDefaultSize) {
            return mStoresDefault.get(position - 1);
        }
        if (position > storesDefaultSize) {
            return mStoresAdded.get(position - storesDefaultSize - NUMBER_OF_HEADER_VIEWS);
        }

        return "";
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_STORE_ADDED;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns the adjusted position of a store by adding the number of default store and the
     * number of header rows to the position in the stores added list.
     *
     * @param position the position of store in the stores added list
     * @return the adjusted position of the store
     */
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

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull final ViewGroup parent) {
        final int rowType = getItemViewType(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            if (rowType == TYPE_HEADER) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        ch.giantific.qwittig.ui.adapters.rows.HeaderRow.VIEW_RESOURCE, parent, false);

                viewHolder = new HeaderRow(convertView);
            } else {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        VIEW_RESOURCE, parent, false);

                viewHolder = new StoreRow(convertView);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (rowType == TYPE_HEADER) {
            String header = position == 0 ? mContext.getString(R.string.header_stores_default) :
                    mContext.getString(R.string.header_stores_own);
            ((HeaderRow) viewHolder).setHeader(header);
        } else {
            StoreRow storeRow = (StoreRow) viewHolder;

            storeRow.setClickListener(((ListView) parent), position);
            String store = getItem(position);
            storeRow.setName(store);
            storeRow.setFavorite(mStoresFavorites.contains(store));
        }

        return convertView;
    }

    /**
     * Provides an abstract base class for the rows in this adapter in order to be able to
     * reference it easily in <code>getView</code>
     */
    private abstract static class ViewHolder {
        // empty stub placeholder
    }

    /**
     * Provides an adapter row that displays a store's name and checkbox to mark it as favorited or
     * not.
     * <p/>
     * Subclass of {@link ViewHolder}.
     */
    private static class StoreRow extends ViewHolder {
        private TextView mTextViewName;
        private CheckBox mCheckBoxFavorite;

        /**
         * Constructs a new {@link StoreRow}.
         *
         * @param view the inflated view
         */
        public StoreRow(@NonNull View view) {
            mTextViewName = (TextView) view.findViewById(R.id.tv_store_name);
            mCheckBoxFavorite = (CheckBox) view.findViewById(R.id.cb_store_favorite);
        }

        /**
         * Sets the click listener on the view.
         * <p/>
         * TODO: check if this is really the best way to achieve this
         *
         * @param listView the view to set the click listener on
         * @param position the adapter position of the view
         */
        public void setClickListener(@NonNull final ListView listView, final int position) {
            mCheckBoxFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listView.performItemClick(v, position, 0);
                }
            });
        }

        /**
         * Sets the name of the store.
         *
         * @param name the name to set
         */
        public void setName(@NonNull String name) {
            mTextViewName.setText(name);
        }

        /**
         * Marks the store as a favorite or not
         *
         * @param isFavorite whether the store should be marked as a favorite or not
         */
        public void setFavorite(boolean isFavorite) {
            mCheckBoxFavorite.setChecked(isFavorite);
        }
    }

    /**
     * Provides an adapter row that displays a header.
     * <p/>
     * Subclass of {@link ViewHolder}.
     */
    private static class HeaderRow extends ViewHolder {
        private TextView mTextViewHeader;

        /**
         * Constructs a new {@link HeaderRow}.
         *
         * @param view the inflated view
         */
        public HeaderRow(@NonNull View view) {
            mTextViewHeader = (TextView) view.findViewById(R.id.tv_header);
        }

        /**
         * Sets the header text.
         *
         * @param header the text to set in the header
         */
        public void setHeader(@NonNull String header) {
            mTextViewHeader.setText(header);
        }
    }
}
