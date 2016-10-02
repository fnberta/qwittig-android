/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.viewmodels.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;

/**
 * Defines the view model for a row item on the task list screen.
 */
public interface BaseAssignmentItemViewModel extends ChildItemViewModel {

    @ViewType
    int getViewType();

    @IntDef({ViewType.HEADER_MY, ViewType.HEADER_GROUP, ViewType.ASSIGNMENT})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
        int HEADER_MY = 1;
        int HEADER_GROUP = 2;
        int ASSIGNMENT = 3;
    }
}
