/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Handles the display of open drafts.
 * <p/>
 * Subclass of {@link BaseAdapter}.
 */
public class DraftsAdapter extends BaseAdapter {

    private static final int VIEW_RESOURCE = R.layout.row_drafts;
    private List<ParseObject> mDrafts;
    private String mCurrentGroupCurrency;

    /**
     * Constructs a new {@link DraftsAdapter}.
     *
     * @param drafts the drafts to to display
     */
    public DraftsAdapter(@NonNull List<ParseObject> drafts) {
        super();

        mDrafts = drafts;
    }

    @Override
    public int getCount() {
        return mDrafts.size();
    }

    @Override
    public Object getItem(int position) {
        return mDrafts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sets the current group currency field. As long this is not set, nothing will be displayed
     * in the adapter.
     *
     * @param currentGroupCurrency the currency code to set
     */
    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull final ViewGroup parent) {
        final DraftRow draftRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent,
                    false);
            draftRow = new DraftRow(convertView);
            convertView.setTag(draftRow);
        } else {
            draftRow = (DraftRow) convertView.getTag();
        }

        Purchase draft = (Purchase) mDrafts.get(position);

        draftRow.setDate(draft.getDate());
        draftRow.setStore(draft.getStore());

        double totalPrice = draft.getTotalPrice();
        draftRow.setTotal(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));

        return convertView;
    }

    /**
     * Provides an adapter row that displays drafts.
     */
    private static class DraftRow {
        private TextView mTextViewDate;
        private TextView mTextViewStore;
        private TextView mTestViewTotal;

        /**
         * Constructs a new {@link DraftRow}.
         *
         * @param view the inflated view
         */
        public DraftRow(@NonNull View view) {
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
    }
}
