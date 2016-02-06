/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

/**
 * Created by fabio on 27.01.16.
 */
public interface PurchaseAddEditItemUsersClickListener {

    void onTooFewUsersSelected();

    void onRowItemUserClick(int position);
}
