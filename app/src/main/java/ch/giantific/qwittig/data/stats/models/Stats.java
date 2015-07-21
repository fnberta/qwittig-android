package ch.giantific.qwittig.data.stats.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Stats {

    @SerializedName("numberOfUnits")
    private int mNumberOfUnits;
    @SerializedName("members")
    private List<Member> mMembers = new ArrayList<>();
    @SerializedName("group")
    private Group mGroup;

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

    public static class Group {

        @SerializedName("groupId")
        private String mGroupId;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

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
    }

    /**
     * Created by fabio on 19.07.15.
     */
    public static class Member {

        @SerializedName("memberId")
        private String mMemberId;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

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
    }

    public static class Unit {

        @SerializedName("identifier")
        private String mIdentifier;
        @SerializedName("total")
        private float mTotal;
        @SerializedName("average")
        private float mAverage;

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
    }
}