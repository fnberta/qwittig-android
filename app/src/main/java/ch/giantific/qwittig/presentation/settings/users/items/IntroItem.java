/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.os.Parcel;

/**
 * Created by fabio on 07.02.16.
 */
public class IntroItem implements SettingsUsersItem {


    public static final Creator<IntroItem> CREATOR = new Creator<IntroItem>() {
        @Override
        public IntroItem createFromParcel(Parcel in) {
            return new IntroItem(in);
        }

        @Override
        public IntroItem[] newArray(int size) {
            return new IntroItem[size];
        }
    };

    public IntroItem() {
    }

    private IntroItem(Parcel in) {
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
