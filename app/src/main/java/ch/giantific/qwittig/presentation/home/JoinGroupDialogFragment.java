/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseDialogFragment;

/**
 * Provides a dialog that asks the user if he wants to join a specific group he was invited to.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class JoinGroupDialogFragment extends BaseDialogFragment<JoinGroupDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = JoinGroupDialogFragment.class.getCanonicalName();
    private static final String KEY_GROUP_NAME = "GROUP_NAME";
    private static final String KEY_INVITER_NICKNAME = "INVITER_NICKNAME";

    /**
     * Returns a new instance of {@link JoinGroupDialogFragment}.
     *
     * @param fm        the fragment manager to use for the transaction
     * @param groupName the name of the group the user was invited to
     * @return a new instance of {@link JoinGroupDialogFragment}
     */
    @NonNull
    public static JoinGroupDialogFragment display(@NonNull FragmentManager fm,
                                                  @NonNull String groupName,
                                                  @NonNull String inviterNickname) {
        final JoinGroupDialogFragment dialog = new JoinGroupDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_GROUP_NAME, groupName);
        args.putString(KEY_INVITER_NICKNAME, inviterNickname);
        dialog.setArguments(args);
        dialog.show(fm, DIALOG_TAG);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String inviterNickname = args.getString(KEY_INVITER_NICKNAME);
        final String groupName = args.getString(KEY_GROUP_NAME);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getString(R.string.dialog_group_join_title, groupName))
                .setMessage(getString(R.string.dialog_group_join_message, inviterNickname, groupName))
                .setPositiveButton(R.string.dialog_positive_join, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.onJoinInvitedGroupSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_negative_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.onDiscardInvitationSelected();
                        dismiss();
                    }
                });
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the join group button.
         */
        void onJoinInvitedGroupSelected();

        /**
         * Handles the click on the discard invitation button.
         */
        void onDiscardInvitationSelected();
    }
}
