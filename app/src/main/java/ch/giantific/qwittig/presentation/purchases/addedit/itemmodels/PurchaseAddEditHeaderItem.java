/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.itemmodels.HeaderItemModelBaseImpl;

/**
 * Provides a header row in the add or edit purchase screen.
 * <p/>
 * Subclass of {@link HeaderItemModelBaseImpl}.
 */
public class PurchaseAddEditHeaderItem extends HeaderItemModelBaseImpl implements PurchaseAddEditItemModel {

    public static final Creator<PurchaseAddEditHeaderItem> CREATOR = new Creator<PurchaseAddEditHeaderItem>() {
        @Override
        public PurchaseAddEditHeaderItem createFromParcel(Parcel source) {
            return new PurchaseAddEditHeaderItem(source);
        }

        @Override
        public PurchaseAddEditHeaderItem[] newArray(int size) {
            return new PurchaseAddEditHeaderItem[size];
        }
    };

    public PurchaseAddEditHeaderItem(@StringRes int header) {
        super(header);
    }

    private PurchaseAddEditHeaderItem(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}