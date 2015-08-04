package ch.giantific.qwittig.ui;


import android.app.Activity;
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
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Currency;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsGroupNewFragment extends SettingsBaseInviteFragment {

    private FragmentInteractionListener mListener;
    private Group mGroupNew;
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
        final Group groupOld = currentUser.getCurrentGroup();
        final Group groupNew = new Group(name, currency);
        currentUser.addGroup(groupNew);
        currentUser.setCurrentGroup(groupNew);
        // We use saveInBackground because we need the object to have an objectId when
        // SettingsFragment starts
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(getActivity(), e);
                    onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));

                    currentUser.removeGroup(groupNew);
                    currentUser.setCurrentGroup(groupOld);
                    return;
                }

                mGroupNew = groupNew;

                // register for notifications for the new group
                ParsePush.subscribeInBackground(groupNew.getObjectId());

                // If user has listed people to invite, invite them, otherwhise finish
                if (mUsersToInviteEmails.isEmpty()) {
                    mListener.finishGroupCreation(mGroupNewName);
                } else {
                    inviteUsers(mGroupNew);
                }
            }
        });
    }

    @Override
    protected void hideProgressCircle() {
        mIsCreatingNew = false;
        mListener.progressCircleHide();
    }

    @Override
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        mListener.finishGroupCreation(mGroupNewName);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void showAccountCreateDialog();

        void progressCircleShow();

        void progressCircleHide();

        void finishGroupCreation(String newGroupName);
    }
}
