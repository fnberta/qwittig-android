/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines an item in the list of the of unpaid compensations screen.
 */
public interface CompsUnpaidBaseItem {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.CREDIT, Type.DEBT})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int CREDIT = 1;
        int DEBT = 2;
    }
}
