package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
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

    public static final String PATH = "identities";
    public static final String PATH_ACTIVE = "active";
    public static final String PATH_GROUP = "group";
    public static final String PATH_GROUP_NAME = "groupName";
    public static final String PATH_GROUP_CURRENCY = "groupCurrency";
    public static final String PATH_USER = "user";
    public static final String PATH_NICKNAME = "nickname";
    public static final String PATH_AVATAR = "avatar";
    public static final String PATH_BALANCE = "balance";
    public static final String PATH_INVITATION_LINK = "invitationLink";
    public static final String NUMERATOR = "num";
    public static final String DENOMINATOR = "den";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_ACTIVE)
    private boolean mActive;
    @PropertyName(PATH_GROUP)
    private String mGroup;
    @PropertyName(PATH_GROUP_NAME)
    private String mGroupName;
    @PropertyName(PATH_GROUP_CURRENCY)
    private String mGroupCurrency;
    @PropertyName(PATH_USER)
    private String mUser;
    @PropertyName(PATH_NICKNAME)
    private String mNickname;
    @PropertyName(PATH_AVATAR)
    private String mAvatar;
    @PropertyName(PATH_BALANCE)
    private Map<String, Long> mBalance;
    @PropertyName(PATH_INVITATION_LINK)
    private String mInvitationLink;

    public Identity() {
        // required for firebase de-/serialization
    }

    public Identity(boolean active, @NonNull String group, @NonNull String groupName,
                    @NonNull String groupCurrency, @Nullable String user,
                    @NonNull String nickname, @Nullable String avatar,
                    @NonNull Map<String, Long> balance, @Nullable String invitationLink) {
        mActive = active;
        mGroup = group;
        mGroupName = groupName;
        mGroupCurrency = groupCurrency;
        mUser = user;
        mNickname = nickname;
        mAvatar = avatar;
        mBalance = balance;
        mInvitationLink = invitationLink;
    }

    @Exclude
    public String getId() {
        return mId;
    }

    @Override
    public void setId(@NonNull String id) {
        mId = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(mCreatedAt);
    }

    public boolean isActive() {
        return mActive;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getGroupCurrency() {
        return mGroupCurrency;
    }

    public String getUser() {
        return mUser;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public Map<String, Long> getBalance() {
        return mBalance;
    }

    @Exclude
    public BigFraction getBalanceFraction() {
        return new BigFraction(mBalance.get(NUMERATOR), mBalance.get(DENOMINATOR));
    }

    public String getInvitationLink() {
        return mInvitationLink;
    }

    @Exclude
    public boolean isPending() {
        return !TextUtils.isEmpty(mInvitationLink);
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_ACTIVE, mActive);
        result.put(PATH_GROUP, mGroup);
        result.put(PATH_GROUP_NAME, mGroupName);
        result.put(PATH_GROUP_CURRENCY, mGroupCurrency);
        result.put(PATH_USER, mUser);
        result.put(PATH_NICKNAME, mNickname);
        result.put(PATH_AVATAR, mAvatar);
        result.put(PATH_BALANCE, mBalance);
        result.put(PATH_INVITATION_LINK, mInvitationLink);

        return result;
    }

    @Override
    public int compareTo(@NonNull Identity another) {
        final String nickname = !TextUtils.isEmpty(mNickname) ? mNickname : "n/a";
        final String nicknameAnother = !TextUtils.isEmpty(another.getNickname()) ? another.getNickname() : "n/a";

        return nickname.compareToIgnoreCase(nicknameAnother);
    }
}