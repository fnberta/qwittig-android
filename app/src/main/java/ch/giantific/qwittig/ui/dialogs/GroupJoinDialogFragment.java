package ch.giantific.qwittig.ui.dialogs;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 20.11.14.
 */
public class GroupJoinDialogFragment extends DialogFragment {

    private static final String BUNDLE_GROUP_NAME = "group_name";
    private static final String BUNDLE_INVITE_INITIATOR = "invite_initiator";
    private String mGroupName;
    private String mInviteInitiator;
    private DialogInteractionListener mListener;
    private String mMessage;

    public static GroupJoinDialogFragment newInstance(String groupName, String initiator) {
        GroupJoinDialogFragment fragment = new GroupJoinDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_GROUP_NAME, groupName);
        args.putString(BUNDLE_INVITE_INITIATOR, initiator);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
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
                        mListener.joinInvitedGroup();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_negative_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.discardInvitation();
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.dialog_neutral_decide_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return dialogBuilder.create();
    }

    public interface DialogInteractionListener {
        void joinInvitedGroup();

        void discardInvitation();
    }
}
