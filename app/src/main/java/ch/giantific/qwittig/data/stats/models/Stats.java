package ch.giantific.qwittig.data.stats.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Stats implements Parcelable {

    public static final Parcelable.Creator<Stats> CREATOR = new Parcelable.Creator<Stats>() {
        public Stats createFromParcel(Parcel source) {
            return new Stats(source);
        }

        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };
    @SerializedName("numberOfUnits")
    private int mNumberOfUnits;
    @SerializedName("members")
    private List<Member> mMembers = new ArrayList<>();
    @SerializedName("group")
    private Group mGroup;

    public Stats() {
    }

    protected Stats(Parcel in) {
        this.mNumberOfUnits = in.readInt();
        this.mMembers = in.createTypedArrayList(Member.CREATOR);
        this.mGroup = in.readParcelable(Group.class.getClassLoader());
    }

    public int getNumberOfUnits() {
        return mNumberOfUnits;
    }

    public void setNumberOfUnits(int numberOfUnits) {
        mNumberOfUnits = numberOfUnits;
    }

    public List<Member> getMembers() {
        return mMembers;
    }

    public void setMembers(List<Member> members) {
        mMembers = members;
    }

    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group group) {
        mGroup = group;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNumberOfUnits);
        dest.writeTypedList(mMembers);
        dest.writeParcelable(this.mGroup, 0);
    }

    public static class Group implements Parcelable {

        public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
            public Group createFromParcel(Parcel source) {
                return new Group(source);
            }

            public Group[] newArray(int size) {
                return new Group[size];
            }
        };
        @SerializedName("groupId")
        private String mGroupId;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

        public Group() {
        }

        protected Group(Parcel in) {
            this.mGroupId = in.readString();
            this.mUnits = in.createTypedArrayList(Unit.CREATOR);
        }

        public String getGroupId() {
            return mGroupId;
        }

        public void setGroupId(String groupId) {
            mGroupId = groupId;
        }

        public List<Unit> getUnits() {
            return mUnits;
        }

        public void setUnits(List<Unit> units) {
            mUnits = units;
        }

        @Override
        public String toString() {
            return getGroupId();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mGroupId);
            dest.writeTypedList(mUnits);
        }
    }

    /**
     * Created by fabio on 19.07.15.
     */
    public static class Member implements Parcelable {

        public static final Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {
            public Member createFromParcel(Parcel source) {
                return new Member(source);
            }

            public Member[] newArray(int size) {
                return new Member[size];
            }
        };
        @SerializedName("memberId")
        private String mMemberId;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

        public Member() {
        }

        protected Member(Parcel in) {
            this.mMemberId = in.readString();
            this.mUnits = in.createTypedArrayList(Unit.CREATOR);
        }

        public String getMemberId() {
            return mMemberId;
        }

        public void setMemberId(String name) {
            mMemberId = name;
        }

        public List<Unit> getUnits() {
            return mUnits;
        }

        public void setUnits(List<Unit> units) {
            mUnits = units;
        }

        @Override
        public String toString() {
            return getMemberId();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mMemberId);
            dest.writeTypedList(mUnits);
        }
    }

    public static class Unit implements Parcelable {

        public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {
            public Unit createFromParcel(Parcel source) {
                return new Unit(source);
            }

            public Unit[] newArray(int size) {
                return new Unit[size];
            }
        };
        @SerializedName("identifier")
        private String mIdentifier;
        @SerializedName("total")
        private float mTotal;
        @SerializedName("average")
        private float mAverage;

        public Unit() {
        }

        protected Unit(Parcel in) {
            this.mIdentifier = in.readString();
            this.mTotal = in.readFloat();
            this.mAverage = in.readFloat();
        }

        public String getIdentifier() {
            return mIdentifier;
        }

        public void setIdentifier(String identifier) {
            mIdentifier = identifier;
        }

        public float getTotal() {
            return mTotal;
        }

        public void setTotal(float total) {
            mTotal = total;
        }

        public float getAverage() {
            return mAverage;
        }

        public void setAverage(float average) {
            mAverage = average;
        }

        @Override
        public String toString() {
            return getIdentifier();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mIdentifier);
            dest.writeFloat(this.mTotal);
            dest.writeFloat(this.mAverage);
        }
    }
}