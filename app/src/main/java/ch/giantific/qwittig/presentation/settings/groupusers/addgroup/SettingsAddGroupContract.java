/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.models.Currency;

/**
 * Defines an observable view model for the addItemAtPosition new group settings screen.
 */
public interface SettingsAddGroupContract {

    interface Presenter extends BasePresenter<ViewListener> {

        List<Currency> getSupportedCurrencies();

        void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onCreateClick(View view);
    }

    interface ViewListener extends BaseView {

        void setScreenResult(@NonNull String name);

        void showAddUsers();
    }
}
