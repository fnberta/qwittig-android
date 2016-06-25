/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowItemModelBaseImpl;

/**
 * Provides an implementation of the {@link TaskDetailsItemModel} interface for a header row.
 */
@SuppressLint("ParcelCreator")
public class TaskDetailsHeaderItem extends HeaderRowItemModelBaseImpl implements TaskDetailsItemModel {

    public TaskDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
