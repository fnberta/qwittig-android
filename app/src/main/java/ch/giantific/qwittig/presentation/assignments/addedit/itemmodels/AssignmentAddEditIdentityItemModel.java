/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.itemmodels;

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
public class AssignmentAddEditIdentityItemModel extends BaseObservable implements Parcelable,
        Comparable<AssignmentAddEditIdentityItemModel> {

    public static final Creator<AssignmentAddEditIdentityItemModel> CREATOR = new Creator<AssignmentAddEditIdentityItemModel>() {
        @Override
        public AssignmentAddEditIdentityItemModel createFromParcel(Parcel source) {
            return new AssignmentAddEditIdentityItemModel(source);
        }

        @Override
        public AssignmentAddEditIdentityItemModel[] newArray(int size) {
            return new AssignmentAddEditIdentityItemModel[size];
        }
    };
    private final String identityId;
    private final String nickname;
    private final String avatar;
    private boolean selected;

    public AssignmentAddEditIdentityItemModel(@NonNull Identity identity, boolean selected) {
        identityId = identity.getId();
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        this.selected = selected;
    }

    private AssignmentAddEditIdentityItemModel(Parcel in) {
        identityId = in.readString();
        nickname = in.readString();
        avatar = in.readString();
        selected = in.readByte() != 0;
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
    public int compareTo(@NonNull AssignmentAddEditIdentityItemModel o) {
        return nickname.compareToIgnoreCase(o.getNickname());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identityId);
        dest.writeString(nickname);
        dest.writeString(avatar);
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
    }
}
