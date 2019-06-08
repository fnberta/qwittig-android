/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items;

import android.databinding.Observable;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public interface BaseHelpFeedbackItemViewModel extends Observable {

    @ViewType
    int getViewType();

    @IntDef({ViewType.HELP_FEEDBACK, ViewType.HEADER})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
        int HEADER = 1;
        int HELP_FEEDBACK = 2;
    }
}
