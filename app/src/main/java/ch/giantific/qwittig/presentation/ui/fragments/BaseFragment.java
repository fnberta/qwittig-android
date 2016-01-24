/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.ViewModel;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;

/**
 * Provides an abstract base class for fragments to house commonly used methods.
 * <p/>
 * Currently only incorporates a base interface for communication with an activity.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseFragment<T extends ViewModel, S extends BaseFragment.ActivityListener>
        extends Fragment
        implements ViewModel.ViewListener {

    private static final String ACCOUNT_CREATE_DIALOG = "ACCOUNT_CREATE_DIALOG";
    private static final String CREATE_GROUP_DIALOG = "CREATE_GROUP_DIALOG";
    S mActivity;
    T mViewModel;

    public BaseFragment() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (S) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mViewModel.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setViewModelToActivity();
    }

    protected abstract void setViewModelToActivity();

    @SuppressWarnings("unchecked")
    @Override
    public void onStart() {
        super.onStart();

        mViewModel.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.detachView();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public boolean isNetworkAvailable() {
        return Utils.isNetworkAvailable(getActivity());
    }

    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(getSnackbarView(), resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull String... args) {
        Snackbar.make(getSnackbarView(), getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {
        Snackbar.make(getSnackbarView(), resId, Snackbar.LENGTH_LONG)
                .setAction(action.getActionText(), action)
                .show();
    }

    protected abstract View getSnackbarView();

    @Override
    public void showCreateAccountDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), ACCOUNT_CREATE_DIALOG);
    }

    @Override
    public void showCreateGroupDialog(@StringRes int message) {
        GroupCreateDialogFragment groupCreateDialogFragment = GroupCreateDialogFragment.newInstance(message);
        groupCreateDialogFragment.show(getFragmentManager(), CREATE_GROUP_DIALOG);
    }

    @Override
    public void removeWorker(@NonNull String workerTag) {
        WorkerUtils.removeWorker(getFragmentManager(), workerTag);
    }

    public interface ActivityListener {
    }
}
