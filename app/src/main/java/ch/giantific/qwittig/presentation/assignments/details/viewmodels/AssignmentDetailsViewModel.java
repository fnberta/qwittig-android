/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsContract;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Provides an implementation of the {@link AssignmentDetailsContract} interface.
 */
public class AssignmentDetailsViewModel extends BaseObservable
        implements LoadingViewModel, EmptyViewModel, Parcelable {

    public static final Parcelable.Creator<AssignmentDetailsViewModel> CREATOR = new Parcelable.Creator<AssignmentDetailsViewModel>() {
        @Override
        public AssignmentDetailsViewModel createFromParcel(Parcel source) {
            return new AssignmentDetailsViewModel(source);
        }

        @Override
        public AssignmentDetailsViewModel[] newArray(int size) {
            return new AssignmentDetailsViewModel[size];
        }
    };
    private boolean loading;
    private boolean empty;
    private String title;
    @StringRes
    private int timeFrame;
    private SpannableStringBuilder identitiesText;
    private boolean responsible;

    public AssignmentDetailsViewModel(boolean loading) {
        this.empty = true;
        this.loading = loading;
    }

    private AssignmentDetailsViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
        this.empty = in.readByte() != 0;
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @StringRes
    @Bindable
    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(@StringRes int timeFrame) {
        this.timeFrame = timeFrame;
        notifyPropertyChanged(BR.timeFrame);
    }

    @Bindable
    public SpannableStringBuilder getIdentitiesText() {
        return identitiesText;
    }

    public void setIdentitiesText(@NonNull SpannableStringBuilder identitiesText) {
        this.identitiesText = identitiesText;
        notifyPropertyChanged(BR.identitiesText);
    }

    @Bindable
    public boolean isResponsible() {
        return responsible;
    }

    public void setResponsible(boolean responsible) {
        this.responsible = responsible;
        notifyPropertyChanged(BR.responsible);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.empty ? (byte) 1 : (byte) 0);
    }
}
