/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class HelpFeedbackItemViewModel extends BaseObservable implements BaseHelpFeedbackItemViewModel {

    public static final Creator<HelpFeedbackItemViewModel> CREATOR = new Creator<HelpFeedbackItemViewModel>() {
        @Override
        public HelpFeedbackItemViewModel createFromParcel(Parcel source) {
            return new HelpFeedbackItemViewModel(source);
        }

        @Override
        public HelpFeedbackItemViewModel[] newArray(int size) {
            return new HelpFeedbackItemViewModel[size];
        }
    };
    @StringRes
    private int title;
    @DrawableRes
    private int icon;

    public HelpFeedbackItemViewModel(@StringRes int title, @DrawableRes int icon) {
        this.title = title;
        this.icon = icon;
    }

    private HelpFeedbackItemViewModel(Parcel in) {
        title = in.readInt();
        icon = in.readInt();
    }

    @Bindable
    @StringRes
    public int getTitle() {
        return title;
    }

    public void setTitle(@StringRes int title) {
        this.title = title;
    }

    @Bindable
    @DrawableRes
    public int getIcon() {
        return icon;
    }

    public void setIcon(@DrawableRes int icon) {
        this.icon = icon;
    }

    @Override
    public int getViewType() {
        return ViewType.HELP_FEEDBACK;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(title);
        dest.writeInt(icon);
    }
}
