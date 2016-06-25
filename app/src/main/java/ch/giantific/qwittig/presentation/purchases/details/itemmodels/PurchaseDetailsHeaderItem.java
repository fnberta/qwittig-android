/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowItemModelBaseImpl;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for a header row.
 */
@SuppressLint("ParcelCreator")
public class PurchaseDetailsHeaderItem extends HeaderRowItemModelBaseImpl implements PurchaseDetailsItemModel {

    public PurchaseDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
