/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the add new group settings screen.
 */
public interface SettingsAddGroupViewModel extends ViewModel<SettingsAddGroupViewModel.ViewListener> {

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    boolean isNameComplete();

    void onNameChanged(CharSequence s, int start, int before, int count);

    List<Currency> getSupportedCurrencies();

    void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onFabCreateClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void setScreenResult(@NonNull String name);

        void showAddUsersFragment();
    }
}
