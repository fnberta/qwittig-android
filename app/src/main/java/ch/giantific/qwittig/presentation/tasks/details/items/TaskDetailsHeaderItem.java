/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.items;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides an implementation of the {@link TaskDetailsBaseItem} interface for a header row.
 */
@SuppressLint("ParcelCreator")
public class TaskDetailsHeaderItem extends HeaderRowViewModelBaseImpl implements TaskDetailsBaseItem {

    public TaskDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
