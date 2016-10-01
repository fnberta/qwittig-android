/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.viewmodels.items;

import android.databinding.Observable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the view model for a row item on the task details screen.
 */
public interface BaseAssignmentDetailsItemViewModel extends Observable {

    @ViewType
    int getViewType();

    @IntDef({ViewType.HEADER, ViewType.HISTORY})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
        int HEADER = 1;
        int HISTORY = 2;
    }
}
