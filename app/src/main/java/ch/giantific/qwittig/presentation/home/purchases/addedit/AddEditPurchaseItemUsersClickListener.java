/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

/**
 * Defines the actions to take when users are clicked.
 */
public interface AddEditPurchaseItemUsersClickListener {

    void onTooFewUsersSelected();

    void onItemRowUserClick(int position);
}
