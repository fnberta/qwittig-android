/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

/**
 * Created by fabio on 27.01.16.
 */
public interface PurchaseAddEditItemUsersClickListener {

    void onTooFewUsersSelected();

    void onRowItemUserClick(int position);
}
