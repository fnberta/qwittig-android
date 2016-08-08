/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides an implementation of the {@link TaskDetailsItemModel} interface for a header row.
 */
@SuppressLint("ParcelCreator")
public class TaskDetailsHeaderItem extends HeaderItemModelBaseImpl implements TaskDetailsItemModel {

    public TaskDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getViewType() {
        return Type.HEADER;
    }

    @Override
    public int getEventType() {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }
}
