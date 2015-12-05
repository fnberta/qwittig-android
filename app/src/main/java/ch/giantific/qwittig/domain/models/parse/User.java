/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseConfig;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.parse.ParseConfigUtils;


/**
 * Represents a user in a group that shares their purchases and tasks and wishes to balance
 * everything.
 */
public class User extends ParseUser {

    public static final String CLASS = "_User";
    public static final String IS_DELETED = "isDeleted";
    public static final String NAME = "username";
    public static final String PASSWORD = "password";
    public static final String NICKNAME = "nickname";
    public static final String AVATAR = "avatar";
    public static final String GROUPS = "groups";
    public static final String CURRENT_GROUP = "currentGroup";
    public static final String BALANCE = "balance";
    public static final String STORES_ADDED = "storesAdded";
    public static final String STORES_FAVORITES = "storesFavorites";
    public static final String FREE_PURCHASES_COUNT = "freePurchasesCount";
    public static final String GOOGLE_ID = "googleId";
    public static final String PIN_LABEL = "usersPinLabel";

    public static final String USERNAME_PREFIX_DELETED = "DELETED_";
    public static final String USERNAME_PREFIX_TEST = "TEST_";


    public User() {
        // A default constructor is required.
    }

    public User(String email, String password, String nickname, byte[] avatar) {
        this(email, password, nickname);
        setAvatar(avatar);
    }

    public User(String email, String password, String nickname) {
        this.setUsername(email);
        this.setPassword(password);
        setDeleted(false);
        setNickname(nickname);
        setStoresFavorites(getDefaultStores());
    }

    /**
     * Returns the default stores defined in Parse.com ParseConfigUtils.
     *
     * @return the default stores
     */
    public List<String> getDefaultStores() {
        ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(ParseConfigUtils.DEFAULT_STORES);
    }

    public boolean isDeleted() {
        return getBoolean(IS_DELETED);
    }

    public void setDeleted(boolean isDeleted) {
        put(IS_DELETED, isDeleted);
    }

    public String getNickname() {
        return getString(NICKNAME);
    }

    public void setNickname(String nickname) {
        put(NICKNAME, nickname);
    }

    public Group getCurrentGroup() {
        return (Group) getParseObject(CURRENT_GROUP);
    }

    public void setCurrentGroup(ParseObject currentGroup) {
        put(CURRENT_GROUP, currentGroup);
    }

    @NonNull
    public List<ParseObject> getGroups() {
        List<ParseObject> groups = getList(GROUPS);
        if (groups != null) {
            return groups;
        } else {
            return Collections.emptyList();
        }
    }

    public void setGroups(List<ParseObject> groups) {
        put(GROUPS, groups);
    }

    public byte[] getAvatar() {
        return getBytes(AVATAR);
    }

    public void setAvatar(byte[] avatar) {
        put(AVATAR, avatar);
    }

    @NonNull
    public List<String> getStoresAdded() {
        List<String> storesAdded = getList(STORES_ADDED);
        if (storesAdded != null) {
            Collections.sort(storesAdded, String.CASE_INSENSITIVE_ORDER);
            return storesAdded;
        }

        return Collections.emptyList();
    }

    public void setStoresAdded(List<String> storesAdded) {
        put(STORES_ADDED, storesAdded);
    }

    public List<String> getStoresFavorites() {
        List<String> storesFavorites = getList(STORES_FAVORITES);
        if (storesFavorites != null) {
            Collections.sort(storesFavorites, String.CASE_INSENSITIVE_ORDER);
            return storesFavorites;
        }

        return Collections.emptyList();
    }

    public void setStoresFavorites(List<String> storesFavorites) {
        put(STORES_FAVORITES, storesFavorites);
    }

    public int getPremiumCount() {
        return getInt(FREE_PURCHASES_COUNT);
    }

    public void setPremiumCount(int count) {
        put(FREE_PURCHASES_COUNT, count);
    }

    public String getGoogleId() {
        return getString(GOOGLE_ID);
    }

    /**
     * Sets the deleted flag to true and the username to deleted.
     * <p/>
     * The rest of the user fields will get emptied by CloudCode on the server
     */
    public void deleteUserFields() {
        setDeleted(true);
        setUsername(USERNAME_PREFIX_DELETED + UUID.randomUUID().toString());
    }

    /**
     * Sets deleted flag to false and the username to the string provided.
     *
     * @param username the username to set
     */
    public void undeleteUserFields(String username) {
        setDeleted(false);
        setUsername(username);
    }

    /**
     * Returns the nickname or "Me" if it's the current user.
     *
     * @param context     the context to use to get the "Me" string
     * @param currentUser the current user
     * @return the nickname or localized "me"
     */
    public String getNicknameOrMe(@NonNull Context context, @NonNull ParseUser currentUser) {
        String nickname;
        if (this == currentUser) {
            nickname = context.getString(R.string.nickname_me);
        } else {
            nickname = getNickname();
        }
        return nickname;
    }

    public void removeCurrentGroup() {
        remove(CURRENT_GROUP);
    }

    /**
     * Returns the object ids of the user's groups.
     *
     * @return the object ids of the user's groups
     */
    @NonNull
    public List<String> getGroupIds() {
        List<String> groupIds = new ArrayList<>();
        List<ParseObject> groups = getGroups();
        for (ParseObject parseObject : groups) {
            groupIds.add(parseObject.getObjectId());
        }

        return groupIds;
    }

    /**
     * Returns the number of groups a user is in.
     *
     * @return the number of groups a user is in
     */
    public int getGroupsCount() {
        List<ParseObject> groups = getGroups();
        return groups.size();
    }

    public void addGroups(List<ParseObject> groups) {
        addAll(GROUPS, groups);
    }

    public void addGroup(ParseObject group) {
        addUnique(GROUPS, group);
    }

    /**
     * Removes a group from the user's list of groups and also removes the corresponding balance
     * entry
     *
     * @param group the group to remove
     */
    public void removeGroup(ParseObject group) {
        List<ParseObject> groupList = new ArrayList<>();
        groupList.add(group);
        removeAll(GROUPS, groupList);

        removeBalanceForGroup(group);
    }

    private void removeBalanceForGroup(@Nullable ParseObject group) {
        Map<String, List<Integer>> balanceMap = getMap(BALANCE);
        if (balanceMap != null && !balanceMap.isEmpty() && group != null) {
            balanceMap.remove(group.getObjectId());
            put(BALANCE, balanceMap);
        }
    }

    public void removeGroups() {
        remove(GROUPS);
    }

    public void removeAvatar() {
        remove(AVATAR);
    }

    /**
     * Returns the current user's balance for a group as {@link BigFraction}.
     *
     * @param group the group for which the balance should be returned
     * @return the balance for the group
     */
    @NonNull
    public BigFraction getBalance(@Nullable ParseObject group) {
        if (group == null) {
            return BigFraction.ZERO;
        }

        Map<String, List<Number>> balanceMap = getMap(BALANCE);
        if (balanceMap == null) {
            return BigFraction.ZERO;
        }

        List<Number> balanceList = balanceMap.get(group.getObjectId());
        if (balanceList == null) {
            return BigFraction.ZERO;
        }

        long numerator = balanceList.get(0).longValue();
        long denominator = balanceList.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    /**
     * Sets a user's balance for the specified group.
     *
     * @param balance the balance to set
     * @param group   the group for which the balance should be set
     */
    public void setBalance(@NonNull BigFraction balance, @Nullable ParseObject group) {
        BigInteger num = balance.getNumerator();
        BigInteger den = balance.getDenominator();

        List<Long> balanceList = new ArrayList<>();
        balanceList.add(num.longValue());
        balanceList.add(den.longValue());

        Map<String, List<Long>> balanceMap = getMap(BALANCE);
        if (group != null) {
            balanceMap.put(group.getObjectId(), balanceList);
            put(BALANCE, balanceMap);
        }
    }

    public void removeBalance() {
        remove(BALANCE);
    }

    public void addStoreAdded(String store) {
        add(STORES_ADDED, store);
    }

    /**
     * Returns the first entry of an alphabetically sorted list of the user's stores.
     *
     * @return the first store
     */
    public String getStoresFavoriteFirstInList() {
        List<String> stores = getStoresFavorites();
        Collections.sort(stores, String.CASE_INSENSITIVE_ORDER);
        return stores.get(0);
    }

    public void addStoreFavorites(String store) {
        add(STORES_FAVORITES, store);
    }

    public void incrementPremiumCount() {
        increment(FREE_PURCHASES_COUNT);
    }

    public void resetPremiumCount() {
        put(FREE_PURCHASES_COUNT, 0);
    }

    public boolean isGoogleUser() {
        return !TextUtils.isEmpty(getGoogleId());
    }

    public void removeGoogleId() {
        remove(GOOGLE_ID);
    }

    public boolean isFacebookUser() {
        return ParseFacebookUtils.isLinked(this);
    }
}

