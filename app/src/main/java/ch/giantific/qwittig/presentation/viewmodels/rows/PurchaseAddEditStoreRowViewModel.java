/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 24.01.16.
 */
public interface PurchaseAddEditStoreRowViewModel extends Observable {

    @Bindable
    String getStore();

    void onStoreChanged(CharSequence s, int start, int before, int count);
}
