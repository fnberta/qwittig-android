/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a list item in the purchase details screen.
 */
public interface PurchaseDetailsBaseItem {

    @Type
    int getType();

    @IntDef({Type.ITEM, Type.HEADER, Type.IDENTITIES, Type.TOTAL, Type.MY_SHARE, Type.NOTE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int IDENTITIES = 1;
        int ITEM = 2;
        int TOTAL = 3;
        int MY_SHARE = 4;
        int NOTE = 5;
    }
}
