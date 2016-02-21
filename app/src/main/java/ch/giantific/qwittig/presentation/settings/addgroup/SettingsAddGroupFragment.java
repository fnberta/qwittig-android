/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsAddGroupBinding;
import ch.giantific.qwittig.di.components.DaggerSettingsAddGroupComponent;
import ch.giantific.qwittig.di.modules.SettingsAddGroupViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsUsersFragment;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Displays the settings screen that allows the user to create a new group and invite users to it.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsAddGroupFragment extends BaseFragment<SettingsAddGroupViewModel, SettingsAddGroupFragment.ActivityListener>
        implements SettingsAddGroupViewModel.ViewListener {

    public static final String RESULT_DATA_GROUP = "RESULT_DATA_GROUP";
    private static final String ADD_USERS_FRAGMENT = "ADD_USERS_FRAGMENT";
    private FragmentSettingsAddGroupBinding mBinding;
    private ProgressDialog mProgressDialog;

    public SettingsAddGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerSettingsAddGroupComponent.builder()
                .settingsAddGroupViewModelModule(new SettingsAddGroupViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsAddGroupBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, ParseUtils.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spSettingsGroupAddNewCurrency.setAdapter(spinnerCurrencyAdapter);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setGroupNewViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.spSettingsGroupAddNewCurrency;
    }

    @Override
    public void loadAddGroupWorker(@NonNull String name, @NonNull String currency) {
        AddGroupWorker.attach(getFragmentManager(), name, currency);
    }

    @Override
    public void toggleProgressDialog(boolean show) {
        if (show) {
            mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.progress_add_group), true, false);
        } else {
            mProgressDialog.hide();
        }
    }

    @Override
    public void showAddUsersFragment() {
        final SettingsUsersFragment fragment = new SettingsUsersFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.START));
            fragment.setEnterTransition(new Slide(Gravity.END));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, ADD_USERS_FRAGMENT)
                .commit();

        mActivity.setUpIconDone();
    }

//    @Override
//    public void finishScreen(@NonNull String groupName) {
//        final FragmentActivity activity = getActivity();
//        final Intent intentNewGroupName = new Intent();
//        intentNewGroupName.putExtra(RESULT_DATA_GROUP, groupName);
//        activity.setResult(Activity.RESULT_OK, intentNewGroupName);
//        ActivityCompat.finishAfterTransition(activity);
//    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setGroupNewViewModel(@NonNull SettingsAddGroupViewModel viewModel);

        /**
         * Changes the up icon in the action bar to a done icon.
         */
        void setUpIconDone();
    }
}
