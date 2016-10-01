/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditContract;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;

/**
 * Provides an implementation of the {@link AssignmentAddEditContract} interface for the add task screen.
 */
public class AssignmentAddEditViewModel extends BaseObservable
        implements Parcelable, EmptyViewModel {

    public static final Parcelable.Creator<AssignmentAddEditViewModel> CREATOR = new Parcelable.Creator<AssignmentAddEditViewModel>() {
        @Override
        public AssignmentAddEditViewModel createFromParcel(Parcel source) {
            return new AssignmentAddEditViewModel(source);
        }

        @Override
        public AssignmentAddEditViewModel[] newArray(int size) {
            return new AssignmentAddEditViewModel[size];
        }
    };
    private boolean empty;
    private Date deadline;
    private String deadlineFormatted;
    private String title;
    private int timeFrame;
    private int selectedTimeFrame;

    public AssignmentAddEditViewModel(@NonNull Date deadline,
                                      @NonNull String deadlineFormatted) {
        this.deadline = deadline;
        this.deadlineFormatted = deadlineFormatted;
    }

    private AssignmentAddEditViewModel(Parcel in) {
        this.empty = in.readByte() != 0;
        long tmpDeadline = in.readLong();
        this.deadline = tmpDeadline == -1 ? null : new Date(tmpDeadline);
        this.deadlineFormatted = in.readString();
        this.title = in.readString();
        this.timeFrame = in.readInt();
        this.selectedTimeFrame = in.readInt();
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDeadlineFormatted() {
        return deadlineFormatted;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(@NonNull Date deadline, @NonNull String deadlineFormatted) {
        this.deadline = deadline;
        this.deadlineFormatted = deadlineFormatted;
        notifyPropertyChanged(BR.deadlineFormatted);
    }

    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
        notifyPropertyChanged(BR.asNeeded);
    }

    @Bindable
    public boolean isAsNeeded() {
        return timeFrame == R.string.time_frame_as_needed;
    }

    @Bindable
    public int getSelectedTimeFrame() {
        return selectedTimeFrame;
    }

    public void setSelectedTimeFrame(int selectedTimeFrame) {
        this.selectedTimeFrame = selectedTimeFrame;
        notifyPropertyChanged(BR.selectedTimeFrame);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.empty ? (byte) 1 : (byte) 0);
        dest.writeLong(this.deadline != null ? this.deadline.getTime() : -1);
        dest.writeString(this.deadlineFormatted);
        dest.writeString(this.title);
        dest.writeInt(this.timeFrame);
        dest.writeInt(this.selectedTimeFrame);
    }
}
