/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;

/**
 * Defines an observable view model for the navigation drawer.
 */
public interface NavDrawerContract {

    interface Presenter extends BasePresenter<ViewListener> {

        boolean isUserLoggedIn();

        void afterLogout();

        void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onAvatarClick(View view);
    }

    interface ViewListener extends BaseViewListener {
        void clearHeaderIdentities();

        void addHeaderIdentities(@NonNull List<Identity> identities);
    }
}
