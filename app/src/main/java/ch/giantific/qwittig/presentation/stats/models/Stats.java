/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents statistical information for a group.
 */
public class Stats {

    @SerializedName("numberOfUnits")
    private int mNumberOfUnits;
    @SerializedName("members")
    private List<Member> mMembers = new ArrayList<>();
    @SerializedName("group")
    private Group mGroup;

    public Stats() {
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

    /**
     * Represents a group, identified by an id, for which statistical information is available.
     */
    public static class Group {

        @SerializedName("groupId")
        private String mGroupId;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

        public Group() {
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
    }

    /**
     * Represents a user, identified by an id, for which statistical information is available.
     */
    public static class Member {

        @SerializedName("nickname")
        private String mNickname;
        @SerializedName("units")
        private List<Unit> mUnits = new ArrayList<>();

        public Member() {
        }

        public String getNickname() {
            return mNickname;
        }

        public void setNickname(String nickname) {
            mNickname = nickname;
        }

        public List<Unit> getUnits() {
            return mUnits;
        }

        public void setUnits(List<Unit> units) {
            mUnits = units;
        }

        @Override
        public String toString() {
            return mNickname;
        }
    }

    /**
     * Represents a single statistical information entry.
     */
    public static class Unit {

        @SerializedName("identifier")
        private String mIdentifier;
        @SerializedName("total")
        private float mTotal;
        @SerializedName("average")
        private float mAverage;

        public Unit() {
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
    }
}