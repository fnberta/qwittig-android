/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import ch.giantific.qwittig.domain.models.Identity;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Defines a view model for a {@link RecyclerView} row that represents an identity available for
 * selection for a task.
 */
public class AssignmentAddEditIdentityItemViewModel extends BaseObservable implements Parcelable,
        Comparable<AssignmentAddEditIdentityItemViewModel> {

    public static final Creator<AssignmentAddEditIdentityItemViewModel> CREATOR = new Creator<AssignmentAddEditIdentityItemViewModel>() {
        @Override
        public AssignmentAddEditIdentityItemViewModel createFromParcel(Parcel in) {
            return new AssignmentAddEditIdentityItemViewModel(in);
        }

        @Override
        public AssignmentAddEditIdentityItemViewModel[] newArray(int size) {
            return new AssignmentAddEditIdentityItemViewModel[size];
        }
    };
    private final String identityId;
    private final String nickname;
    private final String avatar;
    private boolean selected;

    public AssignmentAddEditIdentityItemViewModel(@NonNull Identity identity, boolean selected) {
        identityId = identity.getId();
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        this.selected = selected;
    }

    private AssignmentAddEditIdentityItemViewModel(Parcel in) {
        identityId = in.readString();
        nickname = in.readString();
        avatar = in.readString();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identityId);
        dest.writeString(nickname);
        dest.writeString(avatar);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getIdentityId() {
        return identityId;
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Bindable
    public float getAlpha() {
        return selected ? 1f : DISABLED_ALPHA;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(@NonNull AssignmentAddEditIdentityItemViewModel o) {
        return nickname.compareToIgnoreCase(o.getNickname());
    }

}
