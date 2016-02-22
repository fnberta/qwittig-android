/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.os.Parcel;

/**
 * Provides an implementation of the {@link SettingsUsersBaseItem} for the intro text row.
 */
public class SettingsUsersIntroItem implements SettingsUsersBaseItem {


    public static final Creator<SettingsUsersIntroItem> CREATOR = new Creator<SettingsUsersIntroItem>() {
        @Override
        public SettingsUsersIntroItem createFromParcel(Parcel in) {
            return new SettingsUsersIntroItem(in);
        }

        @Override
        public SettingsUsersIntroItem[] newArray(int size) {
            return new SettingsUsersIntroItem[size];
        }
    };

    public SettingsUsersIntroItem() {
    }

    private SettingsUsersIntroItem(Parcel in) {
    }

    @Override
    public int getType() {
        return Type.INTRO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
