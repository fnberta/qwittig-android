package ch.giantific.qwittig.presentation.login.profile;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 01.05.16.
 */
public class LoginProfileViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<LoginProfileViewModel> CREATOR = new Creator<LoginProfileViewModel>() {
        @Override
        public LoginProfileViewModel createFromParcel(Parcel in) {
            return new LoginProfileViewModel(in);
        }

        @Override
        public LoginProfileViewModel[] newArray(int size) {
            return new LoginProfileViewModel[size];
        }
    };
    private String avatar;
    private String nickname;
    private boolean validate;

    public LoginProfileViewModel() {
    }

    private LoginProfileViewModel(Parcel in) {
        avatar = in.readString();
        nickname = in.readString();
        validate = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(nickname);
        dest.writeByte((byte) (validate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@NonNull String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
        if (validate) {
            notifyPropertyChanged(BR.nicknameComplete);
        }
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    public boolean isInputValid() {
        setValidate(true);
        return isNicknameComplete();
    }

}
