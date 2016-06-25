/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.itemmodels;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the view model for a row item on the task details screen.
 */
public interface TaskDetailsItemModel {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.HISTORY})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int HISTORY = 1;
    }
}
