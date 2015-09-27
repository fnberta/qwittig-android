package ch.giantific.qwittig.ui;


import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Currency;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.CreateGroupHelper;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsGroupNewFragment extends SettingsBaseInviteFragment {

    private static final String CREATE_GROUP_HELPER = "create_group_helper";
    private FragmentInteractionListener mListener;
    private TextInputLayout mTextInputLayoutName;
    private Spinner mSpinnerCurrency;
    private String mGroupNewName;
    private boolean mIsCreatingNew;

    public void setIsCreatingNew(boolean isCreatingNew) {
        mIsCreatingNew = isCreatingNew;
    }

    public SettingsGroupNewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_group_new, container, false);

        mTextInputLayoutName = (TextInputLayout) rootView.findViewById(R.id.til_settings_group_add_new_name);
        mSpinnerCurrency = (Spinner) rootView.findViewById(R.id.sp_settings_group_add_new_currency);

        findUsersToInviteViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<Currency> spinnerCurrencyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, ParseUtils.getSupportedCurrencies());
        spinnerCurrencyAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCurrency.setAdapter(spinnerCurrencyAdapter);

        setupUsersToInviteRows();
    }

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

    private void createNewGroupWithHelper(String newGroupCurrency) {
        FragmentManager fragmentManager = getFragmentManager();
        CreateGroupHelper createGroupHelper = findInviteHelper(fragmentManager);

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

    private CreateGroupHelper findInviteHelper(FragmentManager fragmentManager) {
        return (CreateGroupHelper) fragmentManager.findFragmentByTag(CREATE_GROUP_HELPER);
    }

    private void removeCreateGroupHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        CreateGroupHelper createGroupHelper = findInviteHelper(fragmentManager);

        if (createGroupHelper != null) {
            fragmentManager.beginTransaction().remove(createGroupHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper fails to create new group
     * @param e
     */
    public void onCreateNewGroupFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeCreateGroupHelper();
    }

    /**
     * Called from activity when helper finished creating new group
     * @param newGroup
     * @param invitingUser if the helper is also inviting users to the new group
     */
    public void onNewGroupCreated(Group newGroup, boolean invitingUser) {
        // register for notifications for the new group
        ParsePush.subscribeInBackground(newGroup.getObjectId());

        if (!invitingUser) {
            mListener.finishGroupCreation(mGroupNewName);
        }
    }

    /**
     * Called from activity when helper finished inviting user
     */
    public void onUsersInvited() {
        mListener.finishGroupCreation(mGroupNewName);
    }

    /**
     * Called from activity when helper fails to invite new users to newly created group
     * @param e
     */
    public void onInviteUsersFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeCreateGroupHelper();

        // TODO: new group is created but users not invited, finish activity but tell the user
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

    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        void progressCircleShow();

        void progressCircleHide();

        void finishGroupCreation(String newGroupName);
    }
}
