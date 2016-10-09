/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

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

    public static final Creator<AssignmentsViewModel> CREATOR = new Creator<AssignmentsViewModel>() {
        @Override
        public AssignmentsViewModel createFromParcel(Parcel in) {
            return new AssignmentsViewModel(in);
        }

        @Override
        public AssignmentsViewModel[] newArray(int size) {
            return new AssignmentsViewModel[size];
        }
    };
    public static final String TAG = AssignmentsViewModel.class.getCanonicalName();
    private boolean loading;
    private boolean empty;
    private AssignmentDeadline deadline;

    public AssignmentsViewModel() {
        this.empty = true;
        this.loading = true;
    }

    private AssignmentsViewModel(Parcel in) {
        loading = in.readByte() != 0;
        empty = in.readByte() != 0;
        deadline = in.readParcelable(AssignmentDeadline.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeParcelable(deadline, flags);
    }

    @Override
    public int describeContents() {
        return 0;
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

}
