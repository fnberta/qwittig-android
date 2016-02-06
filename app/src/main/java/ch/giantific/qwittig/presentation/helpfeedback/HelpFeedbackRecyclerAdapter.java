/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.HelpItem;
import ch.giantific.qwittig.presentation.common.adapters.rows.HeaderRow;

/**
 * Handles the display of help and feedback items.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class HelpFeedbackRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int VIEW_RESOURCE = R.layout.row_help_feedback;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private HelpItem[] mItems;

    /**
     * Constructs a new {@link HelpFeedbackRecyclerAdapter}.
     *
     * @param context  the context to use in the adapter
     * @param items    the help and feedback items to display
     * @param listener the callback for user clicks on an item
     */
    public HelpFeedbackRecyclerAdapter(@NonNull Context context, @NonNull HelpItem[] items,
                                       @NonNull AdapterInteractionListener listener) {

        mContext = context;
        mListener = listener;
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(VIEW_RESOURCE, parent, false);
                return new ItemRow(v, mListener);
            }
            case TYPE_HEADER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(HeaderRow.VIEW_RESOURCE, parent, false);
                return new HeaderRow(v);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        HelpItem item = mItems[position];

        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                ItemRow itemRow = (ItemRow) viewHolder;

                itemRow.setTitleWithDrawable(mContext.getString(item.getTitle()),
                        ContextCompat.getDrawable(mContext, item.getIcon()));
                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                headerRow.setHeader(mContext.getString(item.getTitle()));
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems[position].getIcon() == 0) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    /**
     * Defines the actions to take when the user clicks on an item.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on an item
         *
         * @param position the adapter position of the item
         */
        void onHelpFeedbackItemClicked(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays help and feedback items.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class ItemRow extends RecyclerView.ViewHolder {

        private TextView mTextViewTitle;

        /**
         * Constructs a new {@link ItemRow} and sets the click listener.
         *
         * @param view     the inflated view
         * @param listener the callback for user clicks on the item
         */
        public ItemRow(@NonNull View view, @NonNull final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onHelpFeedbackItemClicked(getAdapterPosition());
                }
            });

            mTextViewTitle = (TextView) view.findViewById(R.id.tv_help_title);
        }

        /**
         * Sets the title of an item and its corresponding icon as a compound drawable
         *
         * @param title    the title to set
         * @param drawable the drawable to set as a compound drawable
         */
        public void setTitleWithDrawable(String title, Drawable drawable) {
            mTextViewTitle.setText(title);
            mTextViewTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }
}
