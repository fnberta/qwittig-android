/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.items.HeaderItemViewModel;

/**
 * Provides a header row in the help and feedback screen.
 */
public class HelpFeedbackHeaderViewModel extends BaseObservable implements HeaderItemViewModel,
        BaseHelpFeedbackItemViewModel {

    public static final Creator<HelpFeedbackHeaderViewModel> CREATOR = new Creator<HelpFeedbackHeaderViewModel>() {
        @Override
        public HelpFeedbackHeaderViewModel createFromParcel(Parcel source) {
            return new HelpFeedbackHeaderViewModel(source);
        }

        @Override
        public HelpFeedbackHeaderViewModel[] newArray(int size) {
            return new HelpFeedbackHeaderViewModel[size];
        }
    };
    @StringRes
    private int header;

    public HelpFeedbackHeaderViewModel(@StringRes int header) {
        this.header = header;
    }

    private HelpFeedbackHeaderViewModel(Parcel in) {
        this.header = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.header);
    }
}
