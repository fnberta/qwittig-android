/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by fabio on 24.01.16.
 */
public interface PurchaseAddCurrencyRowViewModel extends Observable {

    void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);
}
