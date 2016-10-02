/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.BaseAssignmentDetailsItemViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.items.HeaderItemViewModel;

/**
 * Provides an implementation of the {@link BaseAssignmentDetailsItemViewModel} interface for a header item.
 */
public class AssignmentHeaderViewModel extends BaseObservable implements
        HeaderItemViewModel, BaseAssignmentItemViewModel {

    @ViewType
    private final int viewType;
    @StringRes
    private int header;

    public AssignmentHeaderViewModel(@StringRes int header, @ViewType int viewType) {
        this.header = header;
        this.viewType = viewType;
    }

    @Override
    public int getEventType() {
        return EventType.NONE;
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
    public String getId() {
        return String.valueOf(viewType);
    }

    @Override
    @ViewType
    public int getViewType() {
        return viewType;
    }
}
