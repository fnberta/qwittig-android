/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a list item in the add or edit purchase screen.
 */
public interface PurchaseAddEditItemModel extends Parcelable {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.DATE, Type.STORE, Type.ARTICLE, Type.IDENTITIES, Type.ADD_ROW, Type.TOTAL})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int DATE = 1;
        int STORE = 2;
        int ARTICLE = 3;
        int IDENTITIES = 4;
        int ADD_ROW = 5;
        int TOTAL = 6;
    }
}
