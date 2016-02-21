/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Created by fabio on 11.02.16.
 */
public class NoteItem extends BaseObservable implements DetailsItem {

    private final String mPurchaseNote;

    public NoteItem(@NonNull String note) {
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
