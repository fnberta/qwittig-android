/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link BaseRecyclerAdapter}.
 */
public class DraftsRecyclerAdapter extends BaseRecyclerAdapter<ParseObject>
        implements SelectionRecyclerAdapter {

    private static final String LOG_TAG = DraftsRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_drafts;
    private AdapterInteractionListener mListener;
    private List<String> mItemsSelected;

    /**
     * Constructs a new {@link DraftsRecyclerAdapter}.
     *
     * @param drafts   the drafts to display
     * @param listener the callback for user clicks on the drafts
     */
    public DraftsRecyclerAdapter(@NonNull Context context, @NonNull List<ParseObject> drafts,
                                 @NonNull List<String> draftsSelected,
                                 @NonNull AdapterInteractionListener listener) {
        super(context, drafts);

        mItemsSelected = draftsSelected;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent, false);
        return new DraftRow(view, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DraftRow draftRow = (DraftRow) viewHolder;
        Purchase draft = (Purchase) mItems.get(position);

        draftRow.setDate(draft.getDate());
        draftRow.setStore(draft.getStore());
        double totalPrice = draft.getTotalPrice();
        draftRow.setTotal(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));
        draftRow.setIsSelected(isSelected(draft.getDraftId()));
    }

    @Override
    public void toggleSelection(int position) {
        Purchase draft = (Purchase) mItems.get(position);
        String draftId = draft.getDraftId();
        if (mItemsSelected.contains(draftId)) {
            mItemsSelected.remove(draftId);
        } else {
            mItemsSelected.add(draftId);
        }

        notifyItemChanged(position);
    }

    @Override
    public void clearSelection(boolean deleteSelectedItems) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final Purchase draft = (Purchase) mItems.get(i);
            final String draftId = draft.getDraftId();
            if (isSelected(draftId)) {
                mItemsSelected.remove(draftId);

                if (deleteSelectedItems) {
                    draft.unpinInBackground();
                    mItems.remove(i);
                    notifyItemRemoved(i);
                } else {
                    notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public boolean isSelected(String draftId) {
        return mItemsSelected.contains(draftId);
    }

    /**
     * Defines the actions to take when a user clicks on a purchase.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a draft.
         *
         * @param position the adapter position of the draft
         */
        void onDraftRowClick(int position);

        /**
         * Handles the long click on a draft.
         *
         * @param position the adapter position of the draft
         */
        void onDraftRowLongClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a draft.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class DraftRow extends RecyclerView.ViewHolder {

        private TextView mTextViewDate;
        private TextView mTextViewStore;
        private TextView mTestViewTotal;

        /**
         * Constructs a new {@link DraftRow} and sets the click listener.
         *
         * @param view     the inflated row
         * @param listener the callback for user clicks on the purchase
         */
        public DraftRow(@NonNull View view,
                        @NonNull final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDraftRowClick(getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onDraftRowLongClick(getAdapterPosition());
                    return true;
                }
            });

            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewStore = (TextView) view.findViewById(R.id.tv_store);
            mTestViewTotal = (TextView) view.findViewById(R.id.tv_total_value);
        }

        /**
         * Sets the date of the draft.
         *
         * @param date the date to set
         */
        public void setDate(@NonNull Date date) {
            mTextViewDate.setText(DateUtils.formatMonthDayLineSeparated(date));
        }

        /**
         * Sets the store of the draft.
         *
         * @param store the store to set
         */
        public void setStore(@NonNull String store) {
            mTextViewStore.setText(store);
        }

        /**
         * Sets the total price of the draft.
         *
         * @param total the total price to set
         */
        public void setTotal(@NonNull String total) {
            mTestViewTotal.setText(total);
        }

        /**
         * Sets the selection state of the draft.
         *
         * @param isSelected whether the draft is selected or not
         */
        public void setIsSelected(boolean isSelected) {
            itemView.setActivated(isSelected);
        }
    }
}
