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
public class LoginInvitationViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<LoginInvitationViewModel> CREATOR = new Creator<LoginInvitationViewModel>() {
        @Override
        public LoginInvitationViewModel createFromParcel(Parcel in) {
            return new LoginInvitationViewModel(in);
        }

        @Override
        public LoginInvitationViewModel[] newArray(int size) {
            return new LoginInvitationViewModel[size];
        }
    };
    public static final String TAG = LoginInvitationViewModel.class.getCanonicalName();
    private String groupName;
    private String inviterNickname;

    public LoginInvitationViewModel() {
    }

    private LoginInvitationViewModel(Parcel in) {
        groupName = in.readString();
        inviterNickname = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(inviterNickname);
    }

    @Override
    public int describeContents() {
        return 0;
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

}
