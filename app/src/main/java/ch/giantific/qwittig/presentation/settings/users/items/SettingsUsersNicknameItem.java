/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link SettingsUsersBaseItem} for the enter nickname row.
 */
public class SettingsUsersNicknameItem extends BaseObservable implements SettingsUsersBaseItem {

    private AddListener mAddListener;
    private String mNickname;
    private boolean mValidate;

    public SettingsUsersNicknameItem(@NonNull AddListener addListener) {
        mAddListener = addListener;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onAddUserClick(View view) {
        if (validate()) {
            mAddListener.onValidUserEntered(mNickname);
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }

    @Override
    public int getType() {
        return Type.NICKNAME;
    }

    /**
     * Defines the actions to take when the user clicks on add user.
     */
    public interface AddListener {

        /**
         * Adds the entered nickname as a new identity.
         *
         * @param nickname the nickname entered
         */
        void onValidUserEntered(@NonNull String nickname);
    }
}
