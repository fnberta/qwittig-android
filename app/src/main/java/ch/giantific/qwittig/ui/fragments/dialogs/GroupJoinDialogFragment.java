/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that asks the user if he wants to join a specific group he was invited to.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class GroupJoinDialogFragment extends DialogFragment {

    private static final String BUNDLE_GROUP_NAME = "BUNDLE_GROUP_NAME";
    private static final String BUNDLE_INVITE_INITIATOR = "BUNDLE_INVITE_INITIATOR";
    @Nullable
    private String mGroupName;
    @Nullable
    private String mInviteInitiator;
    private DialogInteractionListener mListener;
    private String mMessage;

    /**
     * Returns a new instance of {@link GroupJoinDialogFragment}.
     *
     * @param groupName the name of the group the user was invited to
     * @param initiator the nickname of the user who invited the user
     * @return a new intsance of {@link GroupJoinDialogFragment}
     */
    @NonNull
    public static GroupJoinDialogFragment newInstance(@NonNull String groupName,
                                                      @NonNull String initiator) {
        GroupJoinDialogFragment fragment = new GroupJoinDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_GROUP_NAME, groupName);
        args.putString(BUNDLE_INVITE_INITIATOR, initiator);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mGroupName = getArguments().getString(BUNDLE_GROUP_NAME);
            mInviteInitiator = getArguments().getString(BUNDLE_INVITE_INITIATOR);
        }

        if (TextUtils.isEmpty(mInviteInitiator)) {
            mMessage = getString(R.string.dialog_group_join_message_no_initiator, mGroupName);
        } else {
            mMessage = getString(R.string.dialog_group_join_message, mInviteInitiator, mGroupName);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getString(R.string.dialog_group_join_title, mGroupName))
                .setMessage(mMessage)
                .setPositiveButton(R.string.dialog_positive_join, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onJoinInvitedGroupSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_negative_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDiscardInvitationSelected();
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.dialog_neutral_decide_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
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
