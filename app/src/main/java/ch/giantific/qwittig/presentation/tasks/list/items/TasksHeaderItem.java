/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.items;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsBaseItem;

/**
 * Provides an implementation of the {@link TaskDetailsBaseItem} interface for a header item.
 */
@SuppressLint("ParcelCreator")
public class TasksHeaderItem extends HeaderRowViewModelBaseImpl implements TasksBaseItem {

    public TasksHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
