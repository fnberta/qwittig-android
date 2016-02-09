/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 06.02.16.
 */
public interface SettingsAddGroupViewModel extends ViewModel, AddGroupWorkerListener {

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    boolean isNameComplete();

    void onNameChanged(CharSequence s, int start, int before, int count);

    void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onFabCreateClick(View view);

    interface ViewListener extends ViewModel.ViewListener {

        void loadAddGroupWorker(@NonNull String name, @NonNull String currency);

        void toggleProgressDialog(boolean show);

        void showAddUsersFragment();
    }
}
