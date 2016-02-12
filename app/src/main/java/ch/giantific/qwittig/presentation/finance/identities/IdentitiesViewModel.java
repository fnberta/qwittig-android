/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Created by fabio on 18.01.16.
 */
public interface IdentitiesViewModel extends OnlineListViewModel<Identity>,
        IdentitiesUpdateWorkerListener {

    @Bindable
    String getCurrentUserBalance();

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateUsersWorker();

        void setColorTheme(@NonNull BigFraction balance);
    }
}
