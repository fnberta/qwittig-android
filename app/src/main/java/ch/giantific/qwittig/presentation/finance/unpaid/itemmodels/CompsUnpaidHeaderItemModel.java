/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides an implementation of the {@link CompsUnpaidItemModel} for a header item.
 * <p/>
 * Subclass of {@link HeaderItemModelBaseImpl}.
 */
@SuppressLint("ParcelCreator")
public class CompsUnpaidHeaderItemModel extends HeaderItemModelBaseImpl implements CompsUnpaidItemModel {

    public CompsUnpaidHeaderItemModel(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
