/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by fabio on 24.01.16.
 */
public interface PurchaseAddDateViewModel extends Observable {

    @Bindable
    String getDate();

    void setDate(@NonNull Date date);
}
