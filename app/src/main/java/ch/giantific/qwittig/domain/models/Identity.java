package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Identity implements FirebaseModel, Comparable<Identity> {

    public static final String BASE_PATH = "identities";
    public static final String BASE_PATH_ACTIVE = "active";
    public static final String BASE_PATH_INACTIVE = "inactive";

    public static final String PATH_IS_ACTIVE = "isActive";
    public static final String PATH_GROUP = "group";
    public static final String PATH_GROUP_NAME = "groupName";
    public static final String PATH_GROUP_CURRENCY = "groupCurrency";
    public static final String PATH_USER = "user";
    public static final String PATH_NICKNAME = "nickname";
    public static final String PATH_AVATAR = "avatar";
    public static final String PATH_BALANCE = "balance";
    public static final String NUMERATOR = "num";
    public static final String DENOMINATOR = "den";

    private String id;
    private long createdAt;
    private boolean active;
    private String group;
    private String groupName;
    private String groupCurrency;
    private String user;
    private String nickname;
    private String avatar;
    private Map<String, Long> balance;

    public Identity() {
        // required for firebase de-/serialization
    }

    public Identity(boolean active, @NonNull String group, @NonNull String groupName,
                    @NonNull String groupCurrency, @Nullable String user,
                    @NonNull String nickname, @Nullable String avatar,
                    @NonNull Map<String, Long> balance) {
        this.active = active;
        this.group = group;
        this.groupName = groupName;
        this.groupCurrency = groupCurrency;
        this.user = user;
        this.nickname = nickname;
        this.avatar = avatar;
        this.balance = balance;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Override
    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(createdAt);
    }

    public boolean isActive() {
        return active;
    }

    public String getGroup() {
        return group;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupCurrency() {
        return groupCurrency;
    }

    public String getUser() {
        return user;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public Map<String, Long> getBalance() {
        return balance;
    }

    @Exclude
    public BigFraction getBalanceFraction() {
        return new BigFraction(balance.get(NUMERATOR), balance.get(DENOMINATOR));
    }

    @Exclude
    public boolean isPending() {
        return active && TextUtils.isEmpty(user);
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_IS_ACTIVE, active);
        result.put(PATH_GROUP, group);
        result.put(PATH_GROUP_NAME, groupName);
        result.put(PATH_GROUP_CURRENCY, groupCurrency);
        result.put(PATH_USER, user);
        result.put(PATH_NICKNAME, nickname);
        result.put(PATH_AVATAR, avatar);
        result.put(PATH_BALANCE, balance);

        return result;
    }

    @Override
    public int compareTo(@NonNull Identity another) {
        final String nickname = !TextUtils.isEmpty(this.nickname) ? this.nickname : "n/a";
        final String nicknameAnother = !TextUtils.isEmpty(another.getNickname()) ? another.getNickname() : "n/a";

        return nickname.compareToIgnoreCase(nicknameAnother);
    }
}