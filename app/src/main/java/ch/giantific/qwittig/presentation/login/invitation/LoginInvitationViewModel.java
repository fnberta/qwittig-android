package ch.giantific.qwittig.presentation.login.invitation;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginInvitationViewModel extends BaseObservable implements Parcelable {

    public static final Parcelable.Creator<LoginInvitationViewModel> CREATOR = new Parcelable.Creator<LoginInvitationViewModel>() {
        @Override
        public LoginInvitationViewModel createFromParcel(Parcel source) {
            return new LoginInvitationViewModel(source);
        }

        @Override
        public LoginInvitationViewModel[] newArray(int size) {
            return new LoginInvitationViewModel[size];
        }
    };
    private String groupName;
    private String inviterNickname;

    public LoginInvitationViewModel() {
    }

    private LoginInvitationViewModel(Parcel in) {
        this.groupName = in.readString();
        this.inviterNickname = in.readString();
    }

    @Bindable
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
        notifyPropertyChanged(BR.groupName);
    }

    @Bindable
    public String getInviterNickname() {
        return inviterNickname;
    }

    public void setInviterNickname(@NonNull String inviterNickname) {
        this.inviterNickname = inviterNickname;
        notifyPropertyChanged(BR.inviterNickname);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupName);
        dest.writeString(this.inviterNickname);
    }
}
