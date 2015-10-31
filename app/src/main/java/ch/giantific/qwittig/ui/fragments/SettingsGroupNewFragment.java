/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Currency;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.helpers.group.CreateGroupHelper;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the settings screen that allows the user to create a new group and invite users to it.
 * <p/>
 * Subclass of {@link SettingsBaseInviteFragment}.
 */
public class SettingsGroupNewFragment extends SettingsBaseInviteFragment {

    private static final String CREATE_GROUP_HELPER = "CREATE_GROUP_HELPER";
    private FragmentInteractionListener mListener;
    private TextInputLayout mTextInputLayoutName;
    private Spinner mSpinnerCurrency;
    private String mGroupNewName;
    private boolean mIsCreatingNew;

    public SettingsGroupNewFragment() {
        // Required empty public constructor
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

        setupUsersToInviteRows();
    }

    /**
     * Reads the name the user has entered for the new group, checks if the user already has a
     * group with same name and if not creates the new group with a retained helper fragment.
     */
    public void addNewGroup() {
        final User currentUser = (User) ParseUser.getCurrentUser();

        if (ParseUtils.isTestUser(currentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (mIsCreatingNew) {
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mTextInputLayoutName, getString(R.string.toast_no_connection));
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
        List<ParseObject> groups = currentUser.getGroups();
        for (ParseObject parseObject : groups) {
            Group group = (Group) parseObject;
            if (name.equalsIgnoreCase(group.getName())) {
                groupIsNew = false;
            }
        }
        if (!groupIsNew) {
            MessageUtils.showBasicSnackbar(mTextInputLayoutName, getString(R.string.toast_group_already_in_list));
            return;
        }

        if (!invitedUsersEmailsAreValid()) {
            return;
        }

        mListener.progressCircleShow();
        mIsCreatingNew = true;
        mGroupNewName = name;

        String currency = ((Currency) mSpinnerCurrency.getSelectedItem()).getCode();
        createNewGroupWithHelper(currency);
    }

    private void createNewGroupWithHelper(@NonNull String newGroupCurrency) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment createGroupHelper = HelperUtils.findHelper(fragmentManager, CREATE_GROUP_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (createGroupHelper == null) {
            createGroupHelper = CreateGroupHelper.newInstance(mGroupNewName, newGroupCurrency,
                    mUsersToInviteEmails);

            fragmentManager.beginTransaction()
                    .add(createGroupHelper, CREATE_GROUP_HELPER)
                    .commit();
        }
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown during the process
     */
    public void onCreateNewGroupFailed(int errorCode) {
        onInviteError(errorCode, CREATE_GROUP_HELPER);
    }

    /**
     * Adds the object id of the new group to the {@link ParseInstallation} object and finishes if
     * the user has not entered users to invite.
     *
     * @param newGroup     the newly created troup
     * @param invitingUser whether the helper is also inviting users to the new group
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
     * Passes the error code to the generic error handler, shows the user an error  message and
     * removes the retained helper fragment and loading indicators.
     * <p/>
     * TODO: new group is created but users not invited, finish activity but tell the user
     *
     * @param errorCode the error code of the exception thrown during the process
     */
    public void onInviteUsersFailed(int errorCode) {
        onInviteError(errorCode, CREATE_GROUP_HELPER);
    }

    @Override
    protected void hideProgressCircle() {
        mIsCreatingNew = false;
        mListener.progressCircleHide();
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
         * Indicates to start the loading animation of the {@link FABProgressCircle}.
         */
        void progressCircleShow();

        /**
         * Indicates to hide the loading animation of the {@link FABProgressCircle}.
         */
        void progressCircleHide();

        /**
         * Indicates that the activity should be finished with the name of new group in the result
         * data intent.
         *
         * @param newGroupName the name of the new group
         */
        void finishGroupCreation(@NonNull String newGroupName);
    }
}
