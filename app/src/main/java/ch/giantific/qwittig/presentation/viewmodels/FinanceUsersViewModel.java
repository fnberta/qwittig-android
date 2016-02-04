/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.workerfragments.query.UsersUpdateListener;

/**
 * Created by fabio on 18.01.16.
 */
public interface FinanceUsersViewModel extends OnlineListViewModel<User>,
        UsersUpdateListener {

    @Bindable
    String getCurrentUserBalance();

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateUsersWorker();

        void setColorTheme(@NonNull BigFraction balance);
    }
}
