/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.firstgroup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginFirstGroupBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.models.Currency;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginFirstGroupFragment extends BaseFragment<LoginComponent, LoginFirstGroupContract.Presenter, BaseFragment.ActivityListener<LoginComponent>>
        implements LoginFirstGroupContract.ViewListener {

    @Inject
    LoginFirstGroupViewModel viewModel;
    private FragmentLoginFirstGroupBinding binding;

    public LoginFirstGroupFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginFirstGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
        final ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, presenter.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLoginGroupCurrency.setAdapter(spinnerCurrencyAdapter);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.tilLoginGroupName;
    }
}
