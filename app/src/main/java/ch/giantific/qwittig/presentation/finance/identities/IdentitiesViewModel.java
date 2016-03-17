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
 * Defines an observable view model for the identities list screen.
 */
public interface IdentitiesViewModel extends OnlineListViewModel<Identity> {

    @Bindable
    String getCurrentUserBalance();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void startUpdateIdentitiesService();

        void setColorTheme(@NonNull BigFraction balance);
    }
}
