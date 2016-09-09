/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;

/**
 * Defines an abstract base implementation of the {@link HeaderItemModel}.
 */
public abstract class HeaderItemModelBaseImpl extends BaseObservable
        implements HeaderItemModel, Parcelable {

    @StringRes
    private int headerRes;

    public HeaderItemModelBaseImpl(@StringRes int headerRes) {
        this.headerRes = headerRes;
    }

    protected HeaderItemModelBaseImpl(Parcel in) {
        headerRes = in.readInt();
    }

    @Override
    @Bindable
    @StringRes
    public int getHeader() {
        return headerRes;
    }

    public void setHeader(@StringRes int headerRes) {
        this.headerRes = headerRes;
        notifyPropertyChanged(BR.header);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(headerRes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HeaderItemModelBaseImpl that = (HeaderItemModelBaseImpl) o;

        return headerRes == that.headerRes;

    }

    @Override
    public int hashCode() {
        return headerRes;
    }
}
