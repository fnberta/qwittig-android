/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters.rows;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ch.giantific.qwittig.R;

/**
 * Provides a {@link RecyclerView} row that display a header view.
 * <p/>
 * Subclass of {@link RecyclerView.ViewHolder}.
 */
public class HeaderRow extends RecyclerView.ViewHolder {

    public static final int VIEW_RESOURCE = R.layout.row_header;
    private TextView mTextViewHeader;

    /**
     * Constructs a new {@link HeaderRow}.
     *
     * @param view the inflated view
     */
    public HeaderRow(@NonNull View view) {
        super(view);

        mTextViewHeader = (TextView) view.findViewById(R.id.tv_header);
    }

    /**
     * Sets the header to the specified string.
     *
     * @param header the header to set
     */
    public void setHeader(@NonNull String header) {
        mTextViewHeader.setText(header);
    }

    /**
     * Hides the whole row.
     */
    public void hideRow() {
        itemView.setVisibility(View.GONE);
    }
}
