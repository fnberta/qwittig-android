/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.items;

import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Created by fabio on 11.02.16.
 */
public class HeaderItem extends HeaderRowViewModelBaseImpl implements ListItem {

    public HeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
