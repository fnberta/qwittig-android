/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;

/**
 * Defines an abstract base implementation of the {@link HeaderRowViewModel}.
 */
public abstract class HeaderRowViewModelBaseImpl extends BaseObservable
        implements HeaderRowViewModel, Parcelable {

    @StringRes
    private int mHeader;

    public HeaderRowViewModelBaseImpl(@StringRes int header) {
        mHeader = header;
    }

    protected HeaderRowViewModelBaseImpl(Parcel in) {
        mHeader = in.readInt();
    }

    @Override
    @Bindable
    @StringRes
    public int getHeader() {
        return mHeader;
    }

    public void setHeader(@StringRes int header) {
        mHeader = header;
        notifyPropertyChanged(BR.header);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mHeader);
    }
}
