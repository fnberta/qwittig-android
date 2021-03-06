/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.firstgroup;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.models.Currency;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginFirstGroupContract {

    interface Presenter extends BasePresenter<ViewListener> {

        List<Currency> getSupportedCurrencies();

        void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onDoneClick(View view);
    }

    interface ViewListener extends BaseView {
        // empty
    }
}
