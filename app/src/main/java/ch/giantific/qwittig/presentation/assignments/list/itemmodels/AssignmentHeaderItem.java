/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.itemmodels;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel;
import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides an implementation of the {@link AssignmentDetailsItemModel} interface for a header item.
 */
@SuppressLint("ParcelCreator")
public class AssignmentHeaderItem extends HeaderItemModelBaseImpl implements AssignmentItemModel {

    @Type
    private final int viewType;

    public AssignmentHeaderItem(@StringRes int header, @Type int viewType) {
        super(header);

        this.viewType = viewType;
    }

    @Override
    public int getEventType() {
        return EventType.NONE;
    }

    @Override
    public String getId() {
        return String.valueOf(viewType);
    }

    @Override
    @Type
    public int getViewType() {
        return viewType;
    }
}
