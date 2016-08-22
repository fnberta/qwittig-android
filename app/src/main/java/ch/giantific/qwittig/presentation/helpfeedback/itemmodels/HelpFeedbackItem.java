/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class HelpFeedbackItem extends BaseObservable implements HelpFeedbackItemModel {

    public static final Creator<HelpFeedbackItem> CREATOR = new Creator<HelpFeedbackItem>() {
        @Override
        public HelpFeedbackItem createFromParcel(Parcel source) {
            return new HelpFeedbackItem(source);
        }

        @Override
        public HelpFeedbackItem[] newArray(int size) {
            return new HelpFeedbackItem[size];
        }
    };
    @StringRes
    private int title;
    @DrawableRes
    private int icon;

    public HelpFeedbackItem(@StringRes int title, @DrawableRes int icon) {
        this.title = title;
        this.icon = icon;
    }

    private HelpFeedbackItem(Parcel in) {
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
    public int getType() {
        return Type.HELP_FEEDBACK;
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
