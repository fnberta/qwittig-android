package ch.giantific.qwittig.presentation.settings.general;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 09.10.16.
 */

public class SettingsViewModel implements Parcelable {

    public static final String TAG = SettingsViewModel.class.getCanonicalName();
    public static final Creator<SettingsViewModel> CREATOR = new Creator<SettingsViewModel>() {
        @Override
        public SettingsViewModel createFromParcel(Parcel in) {
            return new SettingsViewModel(in);
        }

        @Override
        public SettingsViewModel[] newArray(int size) {
            return new SettingsViewModel[size];
        }
    };
    private final ArrayList<String> identityIds;
    private final ArrayList<String> groupNames;
    private String currentIdentityId;

    public SettingsViewModel() {
        identityIds = new ArrayList<>();
        groupNames = new ArrayList<>();
    }

    private SettingsViewModel(Parcel in) {
        identityIds = in.createStringArrayList();
        groupNames = in.createStringArrayList();
        currentIdentityId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(identityIds);
        dest.writeStringList(groupNames);
        dest.writeString(currentIdentityId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setIdentityIdsGroupNames(@NonNull List<Identity> identities) {
        identityIds.clear();
        groupNames.clear();
        for (Identity identity : identities) {
            final String identityId = identity.getId();
            identityIds.add(identityId);
            final String groupName = identity.getGroupName();
            groupNames.add(groupName);
        }
    }

    public ArrayList<String> getIdentityIds() {
        return identityIds;
    }

    public ArrayList<String> getGroupNames() {
        return groupNames;
    }

    public String getCurrentIdentityId() {
        return currentIdentityId;
    }

    public void setCurrentIdentityId(String currentIdentityId) {
        this.currentIdentityId = currentIdentityId;
    }
}
