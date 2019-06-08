/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.items.HeaderItemViewModel;

/**
 * Provides an implementation of the {@link BaseAssignmentDetailsItemViewModel} interface for a header row.
 */
public class AssignmentDetailsHeaderItemViewModel extends BaseObservable
        implements HeaderItemViewModel, BaseAssignmentDetailsItemViewModel {

    @StringRes
    private int header;

    public AssignmentDetailsHeaderItemViewModel(@StringRes int header) {
        this.header = header;
    }

    @Override
    @StringRes
    @Bindable
    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
        notifyPropertyChanged(BR.header);
    }

    @Override
    public int getViewType() {
        return ViewType.HEADER;
    }
}
