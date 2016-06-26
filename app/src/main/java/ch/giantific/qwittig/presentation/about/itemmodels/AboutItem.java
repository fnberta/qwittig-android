/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class AboutItem extends BaseObservable implements AboutItemModel {

    public static final Creator<AboutItem> CREATOR = new Creator<AboutItem>() {
        @Override
        public AboutItem createFromParcel(Parcel source) {
            return new AboutItem(source);
        }

        @Override
        public AboutItem[] newArray(int size) {
            return new AboutItem[size];
        }
    };
    @StringRes
    private int mTitle;
    @DrawableRes
    private int mIcon;

    public AboutItem(@StringRes int title, @DrawableRes int icon) {
        mTitle = title;
        mIcon = icon;
    }

    public AboutItem(@StringRes int title) {
        mTitle = title;
    }

    private AboutItem(Parcel in) {
        mTitle = in.readInt();
        mIcon = in.readInt();
    }

    @Bindable
    @StringRes
    public int getTitle() {
        return mTitle;
    }

    public void setTitle(@StringRes int title) {
        mTitle = title;
    }

    @Bindable
    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public void setIcon(@DrawableRes int icon) {
        mIcon = icon;
    }

    @Override
    public int getType() {
        return Type.ABOUT;
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
