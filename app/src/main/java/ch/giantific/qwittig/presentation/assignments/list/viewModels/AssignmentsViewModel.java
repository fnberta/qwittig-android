/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsContract;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Provides an implementation of the {@link AssignmentsContract} interface.
 */
public class AssignmentsViewModel extends BaseObservable
        implements Parcelable, LoadingViewModel, EmptyViewModel {

    public static final Parcelable.Creator<AssignmentsViewModel> CREATOR = new Parcelable.Creator<AssignmentsViewModel>() {
        @Override
        public AssignmentsViewModel createFromParcel(Parcel source) {
            return new AssignmentsViewModel(source);
        }

        @Override
        public AssignmentsViewModel[] newArray(int size) {
            return new AssignmentsViewModel[size];
        }
    };
    private boolean loading;
    private boolean empty;
    private AssignmentDeadline deadline;

    public AssignmentsViewModel(boolean loading, @NonNull AssignmentDeadline deadline) {
        this.empty = true;
        this.loading = loading;
        this.deadline = deadline;
    }

    private AssignmentsViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
        this.empty = in.readByte() != 0;
        this.deadline = in.readParcelable(AssignmentDeadline.class.getClassLoader());
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

    public AssignmentDeadline getDeadline() {
        return deadline;
    }

    public void setDeadline(AssignmentDeadline deadline) {
        this.deadline = deadline;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.empty ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.deadline, flags);
    }
}
