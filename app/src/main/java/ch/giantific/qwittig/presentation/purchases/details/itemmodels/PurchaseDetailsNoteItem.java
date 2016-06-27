/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for the note row.
 */
public class PurchaseDetailsNoteItem extends BaseObservable implements PurchaseDetailsItemModel {

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
