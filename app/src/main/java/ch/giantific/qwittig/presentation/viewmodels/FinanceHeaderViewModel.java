/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 03.02.16.
 */
public interface FinanceHeaderViewModel extends Observable {

    @Bindable
    String getCurrentUserBalance();
}
