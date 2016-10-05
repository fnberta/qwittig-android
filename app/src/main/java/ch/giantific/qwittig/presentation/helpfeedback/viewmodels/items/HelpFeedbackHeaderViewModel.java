/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.items.HeaderItemViewModel;

/**
 * Provides a header row in the help and feedback screen.
 */
public class HelpFeedbackHeaderViewModel extends BaseObservable implements HeaderItemViewModel,
        BaseHelpFeedbackItemViewModel {

    @StringRes
    private int header;

    public HelpFeedbackHeaderViewModel(@StringRes int header) {
        this.header = header;
    }

    @Override
    @Bindable
    @StringRes
    public int getHeader() {
        return header;
    }

    public void setHeader(@StringRes int header) {
        this.header = header;
        notifyPropertyChanged(BR.header);
    }

    @Override
    public int getViewType() {
        return ViewType.HEADER;
    }
}
