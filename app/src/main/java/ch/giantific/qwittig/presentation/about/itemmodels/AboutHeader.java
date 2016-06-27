/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.itemmodels;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides a header row in the help and feedback screen.
 * <p/>
 * Subclass of {@link HeaderItemModelBaseImpl}.
 */
public class AboutHeader extends HeaderItemModelBaseImpl implements AboutItemModel {

    public static final Creator<AboutHeader> CREATOR = new Creator<AboutHeader>() {
        @Override
        public AboutHeader createFromParcel(Parcel source) {
            return new AboutHeader(source);
        }

        @Override
        public AboutHeader[] newArray(int size) {
            return new AboutHeader[size];
        }
    };

    public AboutHeader(@StringRes int header) {
        super(header);
    }

    private AboutHeader(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
