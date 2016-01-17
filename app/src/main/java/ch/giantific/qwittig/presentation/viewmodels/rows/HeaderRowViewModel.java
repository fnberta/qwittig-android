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
public class HeaderRowViewModel extends BaseObservable {

    @StringRes
    private int mHeader;

    /**
     * Constructs a new {@link HeaderRowViewModel}.
     *
     * @param header the header to display
     */
    public HeaderRowViewModel(@StringRes int header) {
        setHeader(header);
    }

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
