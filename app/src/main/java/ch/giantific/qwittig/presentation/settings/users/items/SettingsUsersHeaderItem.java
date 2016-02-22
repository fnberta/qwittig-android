/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides an implementation of {@link SettingsUsersBaseItem} for a header row.
 * <p/>
 * Subclass of {@link HeaderRowViewModelBaseImpl}.
 */
public class SettingsUsersHeaderItem extends HeaderRowViewModelBaseImpl implements SettingsUsersBaseItem {

    public static final Creator<SettingsUsersHeaderItem> CREATOR = new Creator<SettingsUsersHeaderItem>() {
        @Override
        public SettingsUsersHeaderItem createFromParcel(Parcel source) {
            return new SettingsUsersHeaderItem(source);
        }

        @Override
        public SettingsUsersHeaderItem[] newArray(int size) {
            return new SettingsUsersHeaderItem[size];
        }
    };

    public SettingsUsersHeaderItem(@StringRes int header) {
        super(header);
    }

    private SettingsUsersHeaderItem(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
