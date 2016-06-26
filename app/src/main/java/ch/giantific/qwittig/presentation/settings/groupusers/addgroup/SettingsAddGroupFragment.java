/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsAddGroupBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersFragment;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the settings screen that allows the user to create a new group and invite users to it.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsAddGroupFragment extends BaseFragment<SettingsGroupUsersComponent, SettingsAddGroupViewModel, SettingsAddGroupFragment.ActivityListener>
        implements SettingsAddGroupViewModel.ViewListener {

    public static final String RESULT_DATA_GROUP = "RESULT_DATA_GROUP";
    private FragmentSettingsAddGroupBinding mBinding;
    private ProgressDialog mProgressDialog;

    public SettingsAddGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsAddGroupBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
        final ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, MoneyUtils.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spSettingsGroupAddNewCurrency.setAdapter(spinnerCurrencyAdapter);
    }

    @Override
    protected void injectDependencies(@NonNull SettingsGroupUsersComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.etSettingsGroupAddNewName;
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(message), true, false);
    }

    @Override
    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

    @Override
    public void loadAddGroupWorker(@NonNull String name, @NonNull String currency) {
        AddGroupWorker.attach(getFragmentManager(), name, currency);
    }

    @Override
    public void setScreenResult(@NonNull String name) {
        final Intent intentNewGroupName = new Intent();
        intentNewGroupName.putExtra(RESULT_DATA_GROUP, name);
        getActivity().setResult(Activity.RESULT_OK, intentNewGroupName);
    }

    @Override
    public void showAddUsersFragment() {
        final SettingsUsersFragment fragment = new SettingsUsersFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.START));
            setAllowReturnTransitionOverlap(false);
            fragment.setEnterTransition(new Slide(Gravity.END));
            fragment.setAllowEnterTransitionOverlap(false);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, SettingsAddGroupActivity.ADD_USERS_FRAGMENT)
                .commit();

        mActivity.setUpIconAsDone();
    }

    public interface ActivityListener extends BaseFragment.ActivityListener<SettingsGroupUsersComponent> {
        void setUpIconAsDone();
    }
}
