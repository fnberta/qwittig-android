/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.List;

import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Represents a user identity. Every user has one identity for every group he is in, containing
 * his nickname, his avatar and the current balance.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Identity")
public class Identity extends ParseObject implements Comparable<Identity> {

    public static final String CLASS = "Identity";
    public static final String GROUP = "group";
    public static final String ACTIVE = "active";
    public static final String PENDING = "pending";
    public static final String NICKNAME = "nickname";
    public static final String AVATAR = "avatar";
    public static final String AVATAR_LOCAL = "avatarLocal";
    public static final String BALANCE = "balance";
    public static final String INVITATION_LINK = "invitationLink";
    public static final String PIN_LABEL = "identitiesPinLabel";
    public static final String PIN_LABEL_TEMP = "identitiesTempPinLabel";

    public Identity() {
        // A default constructor is required.
    }

    public Identity(@NonNull Group group, @NonNull String nickname, @NonNull ParseFile avatar) {
        this(group, nickname, false);
        setAvatar(avatar);
    }

    public Identity(@NonNull Group group, @NonNull String nickname, boolean pending) {
        setGroup(group);
        setActive(true);
        setPending(pending);
        setNickname(nickname);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull Group group) {
        final ParseACL acl = ParseUtils.getDefaultAcl(group, true);
        setACL(acl);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(@NonNull Group group) {
        put(GROUP, group);
    }

    public boolean isActive() {
        return getBoolean(ACTIVE);
    }

    public void setActive(boolean active) {
        put(ACTIVE, active);
    }

    public boolean isPending() {
        return getBoolean(PENDING);
    }

    public void setPending(boolean pending) {
        put(PENDING, pending);
    }

    public String getNickname() {
        return getString(NICKNAME);
    }

    public void setNickname(@NonNull String nickname) {
        put(NICKNAME, nickname);
    }

    public String getAvatarUrl() {
        final String localPath = getAvatarLocal();
        if (!TextUtils.isEmpty(localPath)) {
            return localPath;
        }

        final ParseFile avatar = getAvatar();
        return avatar != null ? avatar.getUrl() : "";
    }

    public ParseFile getAvatar() {
        return getParseFile(AVATAR);
    }

    public void setAvatar(@NonNull ParseFile avatar) {
        put(AVATAR, avatar);
    }

    public void removeAvatar() {
        remove(AVATAR);
    }

    public String getAvatarLocal() {
        return getString(AVATAR_LOCAL);
    }

    public void setAvatarLocal(@NonNull String avatarPath) {
        put(AVATAR_LOCAL, avatarPath);
    }

    public void removeAvatarLocal() {
        remove(AVATAR_LOCAL);
    }

    @NonNull
    public BigFraction getBalance() {
        final List<Number> numbers = getList(BALANCE);
        if (numbers == null) {
            return BigFraction.ZERO;
        }

        final long numerator = numbers.get(0).longValue();
        final long denominator = numbers.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    public void setBalance(@NonNull BigFraction balance) {
        final BigInteger num = balance.getNumerator();
        final BigInteger den = balance.getDenominator();

        final long[] numbers = new long[]{num.longValue(), den.longValue()};
        put(BALANCE, numbers);
    }

    public String getInvitationLink() {
        return getString(INVITATION_LINK);
    }

    public void setInvitationLink(@NonNull String link) {
        put(INVITATION_LINK, link);
    }

    @Override
    public int compareTo(@NonNull Identity another) {
        final String nicknameLhs = getNickname();
        final String nicknameRhs = another.getNickname();
        String compareLhs = "n/a";
        String compareRhs = "n/a";
        if (!TextUtils.isEmpty(nicknameLhs)) {
            compareLhs = nicknameLhs;
        }
        if (!TextUtils.isEmpty(nicknameRhs)) {
            compareRhs = nicknameRhs;
        }

        return compareLhs.compareToIgnoreCase(compareRhs);
    }
}
