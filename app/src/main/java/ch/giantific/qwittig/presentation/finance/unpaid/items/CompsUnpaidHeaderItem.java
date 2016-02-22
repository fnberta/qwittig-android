/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.items;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides an implementation of the {@link CompsUnpaidBaseItem} for a header item.
 * <p/>
 * Subclass of {@link HeaderRowViewModelBaseImpl}.
 */
@SuppressLint("ParcelCreator")
public class CompsUnpaidHeaderItem extends HeaderRowViewModelBaseImpl implements CompsUnpaidBaseItem {

    public CompsUnpaidHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
