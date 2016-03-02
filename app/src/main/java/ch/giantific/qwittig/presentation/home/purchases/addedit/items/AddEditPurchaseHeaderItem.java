/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides a header row in the add or edit purchase screen.
 * <p/>
 * Subclass of {@link HeaderRowViewModelBaseImpl}.
 */
public class AddEditPurchaseHeaderItem extends HeaderRowViewModelBaseImpl implements AddEditPurchaseBaseItem {

    public static final Creator<AddEditPurchaseHeaderItem> CREATOR = new Creator<AddEditPurchaseHeaderItem>() {
        @Override
        public AddEditPurchaseHeaderItem createFromParcel(Parcel source) {
            return new AddEditPurchaseHeaderItem(source);
        }

        @Override
        public AddEditPurchaseHeaderItem[] newArray(int size) {
            return new AddEditPurchaseHeaderItem[size];
        }
    };

    public AddEditPurchaseHeaderItem(@StringRes int header) {
        super(header);
    }

    private AddEditPurchaseHeaderItem(Parcel in) {
        super(in);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
