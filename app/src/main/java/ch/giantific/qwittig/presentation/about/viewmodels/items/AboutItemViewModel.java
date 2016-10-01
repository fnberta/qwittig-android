/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class AboutItemViewModel extends BaseObservable implements BaseAboutItemViewModel {

    public static final Creator<AboutItemViewModel> CREATOR = new Creator<AboutItemViewModel>() {
        @Override
        public AboutItemViewModel createFromParcel(Parcel source) {
            return new AboutItemViewModel(source);
        }

        @Override
        public AboutItemViewModel[] newArray(int size) {
            return new AboutItemViewModel[size];
        }
    };
    @StringRes
    private int mTitle;
    @DrawableRes
    private int mIcon;

    public AboutItemViewModel(@StringRes int title, @DrawableRes int icon) {
        mTitle = title;
        mIcon = icon;
    }

    public AboutItemViewModel(@StringRes int title) {
        mTitle = title;
    }

    private AboutItemViewModel(Parcel in) {
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
    public int getViewType() {
        return ViewType.ABOUT;
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
