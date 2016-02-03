/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.view.View;

/**
 * Created by fabio on 22.01.16.
 */
public interface HomeViewModel extends ViewModel<HomeViewModel.ViewListener> {

    @Bindable
    boolean isDraftsAvailable();

    void setDraftsAvailable(boolean available);

    boolean updateDraftsAvailable();

    void onFabAddPurchaseManualClick(View view);

    void onFabAddPurchaseAutoClick(View view);

    interface ViewListener extends ViewModel.ViewListener {
        void startPurchaseAddActivity(boolean autoMode);
    }
}
