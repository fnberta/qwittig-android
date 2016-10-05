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

    public static final Creator<AssignmentAddEditViewModel> CREATOR = new Creator<AssignmentAddEditViewModel>() {
        @Override
        public AssignmentAddEditViewModel createFromParcel(Parcel in) {
            return new AssignmentAddEditViewModel(in);
        }

        @Override
        public AssignmentAddEditViewModel[] newArray(int size) {
            return new AssignmentAddEditViewModel[size];
        }
    };
    private final int[] timeFrames = new int[]{
            R.string.time_frame_daily,
            R.string.time_frame_weekly,
            R.string.time_frame_monthly,
            R.string.time_frame_yearly,
            R.string.time_frame_as_needed,
            R.string.time_frame_one_time};
    private boolean empty;
    private Date deadline;
    private String deadlineFormatted;
    private String title;
    private int timeFrame;

    public AssignmentAddEditViewModel(@NonNull Date deadline,
                                      @NonNull String deadlineFormatted) {
        this.deadline = deadline;
        this.deadlineFormatted = deadlineFormatted;
    }

    private AssignmentAddEditViewModel(Parcel in) {
        empty = in.readByte() != 0;
        deadlineFormatted = in.readString();
        title = in.readString();
        timeFrame = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeString(deadlineFormatted);
        dest.writeString(title);
        dest.writeInt(timeFrame);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int[] getTimeFrames() {
        return timeFrames;
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

    public void setTimeFrame(int timeFrame, boolean notify) {
        this.timeFrame = timeFrame;
        notifyPropertyChanged(BR.asNeeded);
        if (notify) {
            notifyPropertyChanged(BR.selectedTimeFrame);
        }
    }

    @Bindable
    public boolean isAsNeeded() {
        return timeFrame == R.string.time_frame_as_needed;
    }

    @Bindable
    public int getSelectedTimeFrame() {
        for (int i = 0; i < timeFrames.length; i++) {
            final int timeFrame = timeFrames[i];
            if (this.timeFrame == timeFrame) {
                return i;
            }
        }

        return -1;
    }

}
