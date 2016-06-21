/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

/**
 * Defines the actions to take when users are clicked.
 */
public interface PurchaseAddEditItemUsersClickListener {

    void onTooFewUsersSelected();

    void onItemRowUserClick(int position);
}
