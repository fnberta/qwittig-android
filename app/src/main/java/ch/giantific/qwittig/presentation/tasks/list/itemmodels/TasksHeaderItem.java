/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowItemModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.details.itemmodels.TaskDetailsItemModel;

/**
 * Provides an implementation of the {@link TaskDetailsItemModel} interface for a header item.
 */
@SuppressLint("ParcelCreator")
public class TasksHeaderItem extends HeaderRowItemModelBaseImpl implements TasksItemModel {

    public TasksHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
