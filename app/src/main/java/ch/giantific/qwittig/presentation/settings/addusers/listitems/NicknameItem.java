/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.listitems;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 07.02.16.
 */
public class NicknameItem extends BaseObservable implements ListItem {

    private AddListener mAddListener;
    private String mNickname;
    private boolean mValidate;

    public void setAddListener(@NonNull AddListener addListener) {
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
        mAddListener.onAddUserClick(this);
    }

    public boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }

    @Override
    public int getType() {
        return Type.NICKNAME;
    }

    public interface AddListener {
        void onAddUserClick(@NonNull NicknameItem nicknameItem);
    }
}
