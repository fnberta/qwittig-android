/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginFirstGroupBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginFirstGroupComponent;
import ch.giantific.qwittig.presentation.login.di.LoginFirstGroupViewModelModule;
import ch.giantific.qwittig.presentation.settings.addgroup.Currency;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginFirstGroupFragment extends BaseFragment<LoginFirstGroupViewModel, BaseFragment.ActivityListener>
        implements LoginFirstGroupViewModel.ViewListener {

    private FragmentLoginFirstGroupBinding mBinding;

    public LoginFirstGroupFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerLoginFirstGroupComponent.builder()
                .loginFirstGroupViewModelModule(new LoginFirstGroupViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginFirstGroupBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, mViewModel.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spLoginGroupCurrency.setAdapter(spinnerCurrencyAdapter);
    }

    @Override
    protected void setViewModelToActivity() {
        // nothing to set
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.tilLoginGroupName;
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }
}
