/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a list item in the add or edit purchase screen.
 */
public interface BasePurchaseAddEditItemViewModel extends Parcelable {

    @ViewType
    int getViewType();

    @IntDef({ViewType.HEADER, ViewType.DATE, ViewType.STORE, ViewType.ARTICLE, ViewType.IDENTITIES,
            ViewType.ADD_ROW, ViewType.TOTAL})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
        int HEADER = 1;
        int DATE = 2;
        int STORE = 3;
        int ARTICLE = 4;
        int IDENTITIES = 5;
        int ADD_ROW = 6;
        int TOTAL = 7;
    }
}
