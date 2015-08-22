package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.listeners.SwipeDismissTouchListener;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 09.06.15.
 */
public abstract class SettingsBaseInviteFragment extends BaseFragment {

    private static final String STATE_ROW_COUNT = "state_row_count";
    List<TextInputLayout> mUsersToInviteFields = new ArrayList<>();
    ArrayList<String> mUsersToInviteEmails = new ArrayList<>();
    private int mInvitedUsersRowCount;
    private LinearLayout mLinearLayoutUsers;
    private Button mButtonAddUser;
    private List<View> mUsersToInviteRows = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mInvitedUsersRowCount = savedInstanceState.getInt(STATE_ROW_COUNT);
        } else {
            mInvitedUsersRowCount = 1;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_ROW_COUNT, mInvitedUsersRowCount);
    }

    final void findUsersToInviteViews(View rootView) {
        mLinearLayoutUsers = (LinearLayout) rootView.findViewById(R.id.ll_users);
        mButtonAddUser = (Button) rootView.findViewById(R.id.bt_user_add);
    }

    final void setupUsersToInviteRows() {
        mButtonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInvitedUsersRowCount++;
                addUserRow(mInvitedUsersRowCount);
            }
        });

        for (int i = 0; i < mInvitedUsersRowCount; i++) {
            addUserRow(i + 1);
        }
    }

    final void addUserRow(int idCounter) {
        View userRow = getActivity().getLayoutInflater()
                .inflate(R.layout.row_settings_user_invite, mLinearLayoutUsers, false);
        userRow.setTag(idCounter - 1);
        userRow.setOnClickListener(null);
        userRow.setOnTouchListener(new SwipeDismissTouchListener(userRow, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        int position = Utils.getViewPositionFromTag(view);

                        mInvitedUsersRowCount--;
                        mUsersToInviteRows.remove(position);
                        mUsersToInviteFields.remove(position);
                        resetIdsAndTags();

                        mLinearLayoutUsers.removeView(view);
                    }
                }));
        TextInputLayout tilEmail = (TextInputLayout) userRow.findViewById(R.id.til_email);
        tilEmail.getEditText().setId(idCounter);

        mUsersToInviteRows.add(userRow);
        mUsersToInviteFields.add(tilEmail);
        mLinearLayoutUsers.addView(userRow);
    }

    final void resetIdsAndTags() {
        for (int i = 0, sizeInvitedUsersRows = mUsersToInviteRows.size(); i < sizeInvitedUsersRows; i++) {
            View userRow = mUsersToInviteRows.get(i);
            userRow.setTag(i);

            TextInputLayout tilEmail = mUsersToInviteFields.get(i);
            tilEmail.getEditText().setId(i + 1);
        }
    }

    final boolean invitedUsersEmailsAreValid() {
        mUsersToInviteEmails.clear();
        boolean allEmailsAreValid = true;

        for (TextInputLayout invitedUser : mUsersToInviteFields) {
            String email = invitedUser.getEditText().getText().toString().trim();
            if (Utils.emailIsValid(email)) {
                mUsersToInviteEmails.add(email);
                invitedUser.setErrorEnabled(false);
            } else {
                invitedUser.setError(getString(R.string.error_email));
                allEmailsAreValid = false;
            }
        }

        return allEmailsAreValid;
    }

    @CallSuper
    void onParseError(String errorMessage) {
        hideProgressCircle();
        MessageUtils.showBasicSnackbar(getView(), errorMessage);
    }

    protected abstract void hideProgressCircle();
}
