/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.viewmodels.items;

import android.databinding.Observable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public interface BaseAboutItemViewModel extends Observable {

    @ViewType
    int getViewType();

    @IntDef({ViewType.ABOUT, ViewType.HEADER})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
        int HEADER = 1;
        int ABOUT = 2;
    }
}
