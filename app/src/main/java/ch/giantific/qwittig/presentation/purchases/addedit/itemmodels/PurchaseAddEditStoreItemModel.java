/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

/**
 * Defines the view model for a store row in the add or edit purchase screen.
 */
public interface PurchaseAddEditStoreItemModel extends Observable {

    @Bindable
    String getStore();

    void setStore(@NonNull String store);
}
