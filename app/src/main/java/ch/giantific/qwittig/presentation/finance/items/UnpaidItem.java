/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 11.02.16.
 */
public interface UnpaidItem {

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
