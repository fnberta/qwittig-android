/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogPromptNicknameBinding;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.BaseDialogFragment;

/**
 * Provides a dialog that allows the user to enter his/her email address in order to request a
 * password reset.
 * <p/>
 * If no valid email address is entered, the dialog is only be dismissed by the cancel action.
 * Therefore overrides the default positive button onClickListener because the default behaviour is
 * to always call dismiss().
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class NicknamePromptDialogFragment extends BaseDialogFragment<NicknamePromptDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = NicknamePromptDialogFragment.class.getCanonicalName();
    private static final String KEY_NICKNAME = "NICKNAME";
    private static final String KEY_POSITION = "POSITION";

    private String nickname;
    private int position;
    private DialogPromptNicknameBinding binding;

    /**
     * Shows a new instance of {@link NicknamePromptDialogFragment}.
     *
     * @param fm       the fragment manager to use for the transaction
     * @param nickname the nickname to show
     */
    public static void display(@NonNull FragmentManager fm, @NonNull String nickname, int position) {
        final DialogFragment dialog = new NicknamePromptDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_NICKNAME, nickname);
        args.putInt(KEY_POSITION, position);
        dialog.setArguments(args);
        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            nickname = args.getString(KEY_NICKNAME, "");
            position = args.getInt(KEY_POSITION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        binding = DialogPromptNicknameBinding.inflate(activity.getLayoutInflater());

        if (!TextUtils.isEmpty(nickname)) {
            binding.etDialogNickname.setText(nickname);
        }

        dialogBuilder
                .setMessage(R.string.dialog_change_nickname_message)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.yes, null)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // nickname was entered. Default behavior is to always call dismiss().
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            final Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nickname = binding.etDialogNickname.getText().toString();
                    if (!TextUtils.isEmpty(nickname)) {
                        binding.tilDialogNickname.setErrorEnabled(false);
                        activity.onValidNicknameEntered(nickname, position);
                        dismiss();
                    } else {
                        binding.tilDialogNickname.setError(getString(R.string.error_nickname));
                    }
                }
            });
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Defines the action to take after the user entered a valid nickname and hit enter.
         *
         * @param nickname the nickname address entered
         * @param position position of the field
         */
        void onValidNicknameEntered(@NonNull String nickname, int position);
    }
}
