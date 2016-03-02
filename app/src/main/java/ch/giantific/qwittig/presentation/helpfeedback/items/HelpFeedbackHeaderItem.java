/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.items;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides a header row in the help and feedback screen.
 * <p/>
 * Subclass of {@link HeaderRowViewModelBaseImpl}.
 */
public class HelpFeedbackHeaderItem extends HeaderRowViewModelBaseImpl implements HelpFeedbackBaseItem {

    public static final Creator<HelpFeedbackHeaderItem> CREATOR = new Creator<HelpFeedbackHeaderItem>() {
        @Override
        public HelpFeedbackHeaderItem createFromParcel(Parcel source) {
            return new HelpFeedbackHeaderItem(source);
        }

        @Override
        public HelpFeedbackHeaderItem[] newArray(int size) {
            return new HelpFeedbackHeaderItem[size];
        }
    };

    public HelpFeedbackHeaderItem(@StringRes int header) {
        super(header);
    }

    private HelpFeedbackHeaderItem(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
