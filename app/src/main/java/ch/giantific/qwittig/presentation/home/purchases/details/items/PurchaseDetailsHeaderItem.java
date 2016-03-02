/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides an implementation of the {@link PurchaseDetailsBaseItem} for a header row.
 */
@SuppressLint("ParcelCreator")
public class PurchaseDetailsHeaderItem extends HeaderRowViewModelBaseImpl implements PurchaseDetailsBaseItem {

    public PurchaseDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
