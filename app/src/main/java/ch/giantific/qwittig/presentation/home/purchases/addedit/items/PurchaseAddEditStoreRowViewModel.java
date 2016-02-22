/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Defines the view model for a store row in the add or edit purchase screen.
 */
public interface PurchaseAddEditStoreRowViewModel extends Observable {

    @Bindable
    String getStore();

    void onStoreChanged(CharSequence s, int start, int before, int count);
}
