/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides an implementation of the {@link AssignmentDetailsItemModel} interface for a header row.
 */
@SuppressLint("ParcelCreator")
public class AssignmentDetailsHeaderItem extends HeaderItemModelBaseImpl
        implements AssignmentDetailsItemModel {

    public AssignmentDetailsHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getViewType() {
        return Type.HEADER;
    }
}
