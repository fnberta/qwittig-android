/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class ActionItem extends BaseObservable implements HelpFeedbackItem {

    public static final Creator<ActionItem> CREATOR = new Creator<ActionItem>() {
        @Override
        public ActionItem createFromParcel(Parcel source) {
            return new ActionItem(source);
        }

        @Override
        public ActionItem[] newArray(int size) {
            return new ActionItem[size];
        }
    };
    @StringRes
    private int mTitle;
    @DrawableRes
    private int mIcon;

    public ActionItem(@StringRes int title, @DrawableRes int icon) {
        mTitle = title;
        mIcon = icon;
    }

    protected ActionItem(Parcel in) {
        mTitle = in.readInt();
        mIcon = in.readInt();
    }

    @Bindable
    public int getTitle() {
        return mTitle;
    }

    public void setTitle(@StringRes int title) {
        mTitle = title;
    }

    @Bindable
    public int getIcon() {
        return mIcon;
    }

    public void setIcon(@DrawableRes int icon) {
        mIcon = icon;
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
        dest.writeInt(mTitle);
        dest.writeInt(mIcon);
    }
}
