/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Provides an implementation of the {@link PurchaseDetailsBaseItem} for the note row.
 */
public class PurchaseDetailsNoteItem extends BaseObservable implements PurchaseDetailsBaseItem {

    private final String mPurchaseNote;

    public PurchaseDetailsNoteItem(@NonNull String note) {
        mPurchaseNote = note;
    }

    @Bindable
    public String getPurchaseNote() {
        return mPurchaseNote;
    }

    @Override
    public int getType() {
        return Type.NOTE;
    }
}
