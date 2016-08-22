/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginFirstGroupViewModel extends ViewModel<LoginFirstGroupViewModel.ViewListener> {

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    String getGroupName();

    void setGroupName(@NonNull String groupName);

    @Bindable
    boolean isGroupNameComplete();

    void onGroupNameChanged(CharSequence s, int start, int before, int count);

    @Bindable
    int getSelectedGroupCurrency();

    void setGroupCurrency(@NonNull String groupCurrency);

    List<Currency> getSupportedCurrencies();

    void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onFabDoneClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
    }
}
