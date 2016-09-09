/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.itemmodels;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.itemmodels.ChildItemModel;

/**
 * Defines the view model for a row item on the task list screen.
 */
public interface AssignmentItemModel extends ChildItemModel {

    @Type
    int getViewType();

    @IntDef({Type.HEADER_MY, Type.HEADER_GROUP, Type.ASSIGNMENT})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER_MY = 0;
        int HEADER_GROUP = 1;
        int ASSIGNMENT = 2;
    }
}
