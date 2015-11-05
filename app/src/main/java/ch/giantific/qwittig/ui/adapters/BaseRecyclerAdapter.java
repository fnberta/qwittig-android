/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Provides a abstract base class for {@link RecyclerView} adapters with a list of items.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = BaseRecyclerAdapter.class.getSimpleName();
    Context mContext;
    List<T> mItems;
    String mCurrentGroupCurrency;

    /**
     * Constructs a new {@link BaseRecyclerAdapter}.
     *
     * @param context the context to use in the adapter
     * @param items   the items to display
     */
    public BaseRecyclerAdapter(@NonNull Context context, @NonNull List<T> items) {
        super();

        mContext = context;
        mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        throw new RuntimeException("there is no type that matches the type " + viewType +
                " + make sure your using types correctly");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Returns the position of the last items in the adapter.
     *
     * @return the position of the last items, -1 if there are no purchases
     */
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    /**
     * Sets the current group currency field. As long this is not set, nothing will be displayed
     * in the adapter.
     *
     * @param currentGroupCurrency the currency code to set
     */
    public void setCurrentGroupCurrency(@NonNull String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }
}
