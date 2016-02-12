/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 11.02.16.
 */
public interface DetailsItem {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.HISTORY})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int HISTORY = 1;
    }
}
