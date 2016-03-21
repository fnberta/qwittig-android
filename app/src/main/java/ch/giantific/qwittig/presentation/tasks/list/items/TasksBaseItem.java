/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the view model for a row item on the task list screen.
 */
public interface TasksBaseItem {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.TASK})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int TASK = 1;
    }
}
