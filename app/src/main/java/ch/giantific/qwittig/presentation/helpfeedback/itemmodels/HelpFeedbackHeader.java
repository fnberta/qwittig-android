/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.itemmodels;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides a header row in the help and feedback screen.
 * <p/>
 * Subclass of {@link HeaderItemModelBaseImpl}.
 */
public class HelpFeedbackHeader extends HeaderItemModelBaseImpl implements HelpFeedbackItemModel {

    public static final Creator<HelpFeedbackHeader> CREATOR = new Creator<HelpFeedbackHeader>() {
        @Override
        public HelpFeedbackHeader createFromParcel(Parcel source) {
            return new HelpFeedbackHeader(source);
        }

        @Override
        public HelpFeedbackHeader[] newArray(int size) {
            return new HelpFeedbackHeader[size];
        }
    };

    public HelpFeedbackHeader(@StringRes int header) {
        super(header);
    }

    private HelpFeedbackHeader(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
