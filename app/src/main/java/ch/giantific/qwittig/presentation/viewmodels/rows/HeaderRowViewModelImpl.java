/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link HeaderRowViewModel} interface.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public class HeaderRowViewModelImpl extends BaseObservable implements HeaderRowViewModel {

    @StringRes
    private int mHeader;

    /**
     * Constructs a new {@link HeaderRowViewModel}.
     *
     * @param header the header to display
     */
    public HeaderRowViewModelImpl(@StringRes int header) {
        mHeader = header;
    }

    @Override
    @StringRes
    @Bindable
    public int getHeader() {
        return mHeader;
    }

    public void setHeader(@StringRes int header) {
        mHeader = header;
        notifyPropertyChanged(BR.header);
    }
}
