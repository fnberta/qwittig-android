/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.items;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Created by fabio on 07.02.16.
 */
public class HeaderItem extends HeaderRowViewModelBaseImpl implements AddUsersItem {

    public static final Creator<HeaderItem> CREATOR = new Creator<HeaderItem>() {
        @Override
        public HeaderItem createFromParcel(Parcel source) {
            return new HeaderItem(source);
        }

        @Override
        public HeaderItem[] newArray(int size) {
            return new HeaderItem[size];
        }
    };

    public HeaderItem(@StringRes int header) {
        super(header);
    }

    protected HeaderItem(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
