/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.parse.ParseConfig;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.viewmodels.SelectionModeViewModel;
import ch.giantific.qwittig.utils.parse.ParseConfigUtils;
import ch.giantific.qwittig.presentation.ui.adapters.rows.HeaderRow;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class StoresRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SelectionModeViewModel {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STORE_ADDED = 1;
    private static final int TYPE_STORE_DEFAULT = 2;
    private static final int NUMBER_OF_HEADER_VIEWS = 2;
    private static final int VIEW_RESOURCE_DEFAULT = R.layout.row_stores_default;
    private static final int VIEW_RESOURCE_ADDED = R.layout.row_stores_added;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private List<String> mStoresDefault;
    private List<String> mStoresAdded;
    private List<String> mStoresFavorites;
    private List<String> mStoresSelected;

    /**
     * Constructs a new {@link StoresRecyclerAdapter}.
     *
     * @param context         the context to use in the adapter
     * @param storesAdded     the stores the user has added himself
     * @param storesFavorites the stores the user has marked as favorite
     * @param listener        the callback for user clicks on the stores
     */
    public StoresRecyclerAdapter(@NonNull Context context, @NonNull List<String> storesAdded,
                                 @NonNull List<String> storesFavorites,
                                 @NonNull List<String> storesSelected,
                                 @NonNull AdapterInteractionListener listener) {
        super();

        mContext = context;
        mStoresAdded = storesAdded;
        mStoresFavorites = storesFavorites;
        mStoresSelected = storesSelected;
        mStoresDefault = getDefaultStores();
        mListener = listener;
    }

    private List<String> getDefaultStores() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        return config.getList(ParseConfigUtils.DEFAULT_STORES);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER: {
                View view = inflater.inflate(HeaderRow.VIEW_RESOURCE, parent, false);
                return new HeaderRow(view);
            }
            case TYPE_STORE_DEFAULT: {
                View view = inflater.inflate(VIEW_RESOURCE_DEFAULT, parent, false);
                return new StoreRow(view, mListener);
            }
            case TYPE_STORE_ADDED: {
                View view = inflater.inflate(VIEW_RESOURCE_ADDED, parent, false);
                return new StoreAddedRow(view, mListener);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                String header = position == 0 ? mContext.getString(R.string.header_stores_default) :
                        mContext.getString(R.string.header_stores_own);
                headerRow.setHeader(header);
                break;
            }
            case TYPE_STORE_DEFAULT: {
                StoreRow storeRow = (StoreRow) viewHolder;
                String store = getItem(position);

                storeRow.setName(store);
                storeRow.setFavorite(mStoresFavorites.contains(store));
                break;
            }
            case TYPE_STORE_ADDED: {
                StoreAddedRow storeRow = (StoreAddedRow) viewHolder;
                String store = getItem(position);

                storeRow.setName(store);
                storeRow.setFavorite(mStoresFavorites.contains(store));
                storeRow.setIsSelected(isSelected(store));
                break;
            }
        }
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

    /**
     * Returns the store for the position. Returns an empty string if the position points to a
     * header view.
     *
     * @param position the position of the store
     * @return the store for the position or an empty string if it is a header
     */
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
    public int getItemCount() {
        return mStoresDefault.size() + mStoresAdded.size() + NUMBER_OF_HEADER_VIEWS;
    }

    @Override
    public void toggleSelection(int position) {
        if (getItemViewType(position) != TYPE_STORE_ADDED) {
            return;
        }

        String store = getItem(position);
        if (mStoresSelected.contains(store)) {
            mStoresSelected.remove(store);
        } else {
            mStoresSelected.add(store);
        }

        notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = mStoresAdded.size() - 1; i >= 0; i--) {
            final String store = mStoresAdded.get(i);
            if (isSelected(store)) {
                mStoresSelected.remove(store);
                int adjustedPosition = getAdjustedStoreAddedPosition(i);
//                if (deleteSelectedItems) {
//                    mStoresAdded.remove(i);
//                    if (mStoresFavorites.contains(store)) {
//                        mStoresFavorites.remove(store);
//                    }
//                    notifyItemRemoved(adjustedPosition);
//
//                    mListener.onStoresSet();
//                } else {
//                    notifyItemChanged(adjustedPosition);
//                }
            }
        }
    }

    @Override
    public boolean isSelected(@NonNull Object item) {
        return mStoresSelected.contains(item);
    }

    /**
     * Defines the actions to take when a user clicks on a purchase.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a store.
         *
         * @param position the adapter position of the store
         */
        void onStoreRowClick(int position);

        /**
         * Handles the long click on a store.
         *
         * @param position the adapter position of the store
         */
        void onStoreRowLongClick(int position);

        /**
         * Handles the long click on a draft.
         *
         * @param position the adapter position of the draft
         */
        void onStoreRowStarClick(int position, CheckBox checkBox);

        /**
         * Handles the saving the stores to the users account.
         */
        void onStoresSet();
    }

    /**
     * Provides a {@link RecyclerView} row that displays a store's name and checkbox to mark it as
     * favored or not.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class StoreRow extends RecyclerView.ViewHolder {

        private TextView mTextViewName;
        private CheckBox mCheckBoxFavorite;

        /**
         * Constructs a new {@link StoreRow} and sets the checkbox click listener.
         *
         * @param view     the inflated row
         * @param listener the callback for user clicks on the store or the star checkbox
         */
        public StoreRow(@NonNull View view,
                        @NonNull final AdapterInteractionListener listener) {
            super(view);

            mTextViewName = (TextView) view.findViewById(R.id.tv_store_name);
            mCheckBoxFavorite = (CheckBox) view.findViewById(R.id.cb_store_favorite);
            mCheckBoxFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onStoreRowStarClick(getAdapterPosition(), mCheckBoxFavorite);
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

    private static class StoreAddedRow extends StoreRow {

        /**
         * Constructs a new {@link StoreAddedRow} and sets the click listeners.
         *
         * @param view     the inflated row
         * @param listener the callback for user clicks on the store or the star checkbox
         */
        public StoreAddedRow(@NonNull View view,
                             @NonNull final AdapterInteractionListener listener) {
            super(view, listener);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onStoreRowClick(getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onStoreRowLongClick(getAdapterPosition());
                    return true;
                }
            });
        }

        /**
         * Sets the selection state of the store.
         *
         * @param isSelected whether the store is selected or not
         */
        public void setIsSelected(boolean isSelected) {
            itemView.setActivated(isSelected);
        }
    }
}
