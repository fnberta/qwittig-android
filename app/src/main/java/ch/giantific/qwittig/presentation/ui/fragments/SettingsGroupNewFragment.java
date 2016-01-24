/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;

import java.util.List;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.workerfragments.group.CreateGroupWorker;
import ch.giantific.qwittig.domain.models.Currency;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the settings screen that allows the user to create a new group and invite users to it.
 * <p/>
 * Subclass of {@link SettingsBaseInviteFragment}.
 */
public class SettingsGroupNewFragment extends SettingsBaseInviteFragment {

    private static final String CREATE_GROUP_WORKER = "CREATE_GROUP_WORKER";
    private FragmentInteractionListener mListener;
    private TextInputLayout mTextInputLayoutName;
    private Spinner mSpinnerCurrency;
    private String mGroupNewName;
    private boolean mIsCreatingNew;

    public SettingsGroupNewFragment() {
        // Required empty public constructor
    }

    public boolean isCreatingNew() {
        return mIsCreatingNew;
    }

    public void setIsCreatingNew(boolean isCreatingNew) {
        mIsCreatingNew = isCreatingNew;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_group_new, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutName = (TextInputLayout) view.findViewById(R.id.til_settings_group_add_new_name);

        mSpinnerCurrency = (Spinner) view.findViewById(R.id.sp_settings_group_add_new_currency);
        ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, ParseUtils.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCurrency.setAdapter(spinnerCurrencyAdapter);

        updateCurrentUserAndGroup();
        setupUsersToInviteRows();
    }

    /**
     * Reads the name the user has entered for the new group, checks if the user already has a
     * group with same name and if not creates the new group with a retained worker fragment.
     */
    public void addNewGroup() {
        if (mIsCreatingNew) {
            return;
        }

        if (!Utils.isNetworkAvailable(getActivity())) {
            Snackbar.make(mTextInputLayoutName, R.string.toast_no_connection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        String name = mTextInputLayoutName.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mTextInputLayoutName.setError(getString(R.string.error_group_name));
            return;
        } else {
            mTextInputLayoutName.setErrorEnabled(false);
        }

        boolean groupIsNew = true;
        List<ParseObject> groups = mCurrentUser.getGroups();
        for (ParseObject parseObject : groups) {
            Group group = (Group) parseObject;
            if (name.equalsIgnoreCase(group.getName())) {
                groupIsNew = false;
            }
        }
        if (!groupIsNew) {
            Snackbar.make(mTextInputLayoutName, R.string.toast_group_already_in_list,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!invitedUsersEmailsAreValid()) {
            return;
        }

        mListener.startProgressAnim();
        mIsCreatingNew = true;
        mGroupNewName = name;

        String currency = ((Currency) mSpinnerCurrency.getSelectedItem()).getCode();
        createNewGroupWithWorker(currency);
    }

    private void createNewGroupWithWorker(@NonNull String newGroupCurrency) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment createGroupWorker = WorkerUtils.findWorker(fragmentManager, CREATE_GROUP_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (createGroupWorker == null) {
            createGroupWorker = CreateGroupWorker.newInstance(mGroupNewName, newGroupCurrency,
                    mUsersToInviteEmails);

            fragmentManager.beginTransaction()
                    .add(createGroupWorker, CREATE_GROUP_WORKER)
                    .commit();
        }
    }

    /**
     * Shows the user an error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    public void onCreateNewGroupFailed(@StringRes int errorMessage) {
        onInviteError(errorMessage, CREATE_GROUP_WORKER);
    }

    /**
     * Adds the object id of the new group to the {@link ParseInstallation} object and finishes if
     * the user has not entered users to invite.
     *
     * @param newGroup     the newly created troup
     * @param invitingUser whether the worker fragment is also inviting users to the new group
     */
    public void onNewGroupCreated(@NonNull Group newGroup, boolean invitingUser) {
        // register for notifications for the new group
        ParsePush.subscribeInBackground(newGroup.getObjectId());

        if (!invitingUser) {
            mListener.finishGroupCreation(mGroupNewName);
        }
    }

    /**
     * Finishes with the name of the new group in the result intent.
     */
    public void onUsersInvited() {
        mListener.finishGroupCreation(mGroupNewName);
    }

    /**
     * Shows the user an error message and removes the retained worker fragment and loading
     * indicators.
     * <p/>
     * TODO: new group is created but users not invited, finish activity but tell the user
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    public void onInviteUsersFailed(@StringRes int errorMessage) {
        onInviteError(errorMessage, CREATE_GROUP_WORKER);
    }

    @Override
    protected void hideProgressCircle() {
        mIsCreatingNew = false;
        mListener.stopProgressAnim();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Indicates to start the loading animation of the {@link FabProgress}.
         */
        void startProgressAnim();

        /**
         * Indicates to hide the loading animation of the {@link FabProgress}.
         */
        void stopProgressAnim();

        /**
         * Indicates that the activity should be finished with the name of new group in the result
         * data intent.
         *
         * @param newGroupName the name of the new group
         */
        void finishGroupCreation(@NonNull String newGroupName);
    }
}
