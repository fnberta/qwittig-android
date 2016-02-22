/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.Date;

/**
 * Defines the view model for a date row in the add or edit purchase screen.
 */
public interface PurchaseAddEditDateRowViewModel extends Observable {

    @Bindable
    String getDate();

    void setDate(@NonNull Date date);

    void onDateClick(View view);
}
