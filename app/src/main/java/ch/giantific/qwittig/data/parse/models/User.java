package ch.giantific.qwittig.data.parse.models;

import android.content.Context;

import com.parse.ParseConfig;
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


/**
 * Created by fabio on 12.10.14.
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
    public static final String PIN_LABEL = "usersPinLabel";

    public static final String USERNAME_PREFIX_DELETED = "DELETED_";
    public static final String USERNAME_PREFIX_TEST = "TEST_";

    public User() {
        // A default constructor is required.
    }

    public User(String email, String password, String nickname) {
        this.setUsername(email);
        this.setPassword(password);
        put(IS_DELETED, false);
        put(NICKNAME, nickname);
        put(STORES_FAVORITES, getDefaultStores());
    }

    public User(String email, String password, String nickname, byte[] avatar) {
        this.setUsername(email);
        this.setPassword(password);
        put(IS_DELETED, false);
        put(NICKNAME, nickname);
        put(AVATAR, avatar);
        put(STORES_FAVORITES, getDefaultStores());
    }

    private List<String> getDefaultStores() {
        ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(Config.DEFAULT_STORES);
    }

    public boolean isDeleted() {
        return getBoolean(IS_DELETED);
    }

    public void setDeleted(boolean isDeleted) {
        put(IS_DELETED, isDeleted);
    }

    /**
     * Sets deleted flag to true, username to deleted and password to empty. NOTE: the rest of the
     * user fields will get emptied by CloudCode on the server
     */
    public void deleteUserFields() {
        setDeleted(true);

        setUsername(USERNAME_PREFIX_DELETED + UUID.randomUUID().toString());
    }

    public String getNicknameOrMe(Context context) {
        String nickname;
        if (this == ParseUser.getCurrentUser()) {
            nickname = context.getString(R.string.nickname_me);
        } else {
            nickname = getString(NICKNAME);
        }
        return nickname;
    }

    public String getNickname() {
        return getString(NICKNAME);
    }

    public void setNickname(String nickname) {
        put(NICKNAME, nickname);
    }

    public void removeNickname() {
        remove(NICKNAME);
    }

    public Group getCurrentGroup() {
        return (Group) getParseObject(CURRENT_GROUP);
    }

    public void setCurrentGroup(ParseObject currentGroup) {
        put(CURRENT_GROUP, currentGroup);
    }

    public void removeCurrentGroup() {
        remove(CURRENT_GROUP);
    }

    public List<ParseObject> getGroups() {
        List<ParseObject> groups = getList(GROUPS);
        if (groups != null) {
            return groups;
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> getGroupIds() {
        List<String> groupIds = new ArrayList<>();
        List<ParseObject> groups = getGroups();
        for (ParseObject parseObject : groups) {
            groupIds.add(parseObject.getObjectId());
        }

        return groupIds;
    }

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

    public void removeGroup(ParseObject group) {
        List<ParseObject> groupList = new ArrayList<>();
        groupList.add(group);
        removeAll(GROUPS, groupList);


        removeBalanceForGroup(group);
    }

    private void removeBalanceForGroup(ParseObject group) {
        Map<String, List<Integer>> balanceMap = getMap(BALANCE);
        if (balanceMap != null && !balanceMap.isEmpty() && group != null) {
            balanceMap.remove(group.getObjectId());
            put(BALANCE, balanceMap);
        }
    }

    public void removeGroups() {
        remove(GROUPS);
    }

    public byte[] getAvatar() {
        return getBytes(AVATAR);
    }

    public void setAvatar(byte[] avatar) {
        put(AVATAR, avatar);
    }

    public void removeAvatar() {
        remove(AVATAR);
    }

    public BigFraction getBalance(ParseObject group) {
        String groupId = "";
        if (group != null) {
            groupId = group.getObjectId();
        }
        Map<String, List<Number>> balanceMap = getMap(BALANCE);
        if (balanceMap == null) {
            return BigFraction.ZERO;
        }

        List<Number> balanceList = balanceMap.get(groupId);
        if (balanceList == null) {
            return BigFraction.ZERO;
        }

        long numerator = balanceList.get(0).longValue();
        long denominator = balanceList.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    public void setBalance(BigFraction balance, ParseObject group) {
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

    public void addStoreAdded(String store) {
        add(STORES_ADDED, store);
    }

    public List<String> getStoresFavorites() {
        List<String> storesFavorites = getList(STORES_FAVORITES);
        if (storesFavorites != null) {
            Collections.sort(storesFavorites, String.CASE_INSENSITIVE_ORDER);
            return storesFavorites;
        }

        return Collections.emptyList();
    }

    public String getStoresFavoriteFirstInList(String otherStore) {
        List<String> stores = getStoresFavorites();
        if (stores.contains(otherStore)) {
            stores.remove(otherStore);
        }
        Collections.sort(stores, String.CASE_INSENSITIVE_ORDER);
        return stores.get(0);
    }

    public void setStoresFavorites(List<String> storesFavorites) {
        put(STORES_FAVORITES, storesFavorites);
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

    public int getPremiumCount() {
        return getInt(FREE_PURCHASES_COUNT);
    }
}

