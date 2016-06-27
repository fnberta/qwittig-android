/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about.itemmodels;

import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public interface AboutItemModel extends Parcelable {

    @Type
    int getType();

    @IntDef({Type.ABOUT, Type.HEADER})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int ABOUT = 1;
    }
}
