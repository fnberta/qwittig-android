/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Defines the view model for a date row in the add or edit purchase screen.
 */
public interface PurchaseAddEditDateItemViewModel extends Observable {

    Date getDate();

    @Bindable
    String getDateFormatted();

    void setDate(@NonNull Date date, @NonNull String dateFormatted);
}
