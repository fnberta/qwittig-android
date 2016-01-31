/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters.rows;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ch.giantific.qwittig.R;

/**
 * Provides a very simple {@link RecyclerView} progress row, used for showing a load more indicator
 * in infinite scrolling.
 * <p/>
 * Subclass of {@link RecyclerView.ViewHolder}.
 */
public class ProgressRow extends RecyclerView.ViewHolder {

    public static final int VIEW_RESOURCE = R.layout.row_generic_progress;

    /**
     * Constructs a new {@link ProgressRow}.
     *
     * @param view the inflated view
     */
    public ProgressRow(@NonNull View view) {
        super(view);
    }
}
