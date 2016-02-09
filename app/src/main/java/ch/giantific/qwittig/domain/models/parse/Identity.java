/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseACL;
import com.parse.ParseClassName;
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
    public static final String ACTIVE = "isActive";
    public static final String PENDING = "pending";
    public static final String NICKNAME = "nickname";
    public static final String AVATAR = "avatar";
    public static final String BALANCE = "balance";
    public static final String PIN_LABEL = "identitiesPinLabel";

    public Identity() {
        // A default constructor is required.
    }

    public Identity(@NonNull Group group, @NonNull String nickname, @NonNull byte[] avatar) {
        this(group, nickname);
        setAvatar(avatar);
    }

    public Identity(@NonNull Group group, @NonNull String nickname) {
        this(group);
        setPending(true);
        setNickname(nickname);
    }

    public Identity(@NonNull Group group) {
        setGroup(group);
        setActive(true);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull Group group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
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

    public byte[] getAvatar() {
        return getBytes(AVATAR);
    }

    public void setAvatar(@NonNull byte[] avatar) {
        put(AVATAR, avatar);
    }

    @NonNull
    public BigFraction getBalance() {
        List<Number> numbers = getList(BALANCE);
        if (numbers == null) {
            return BigFraction.ZERO;
        }

        long numerator = numbers.get(0).longValue();
        long denominator = numbers.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    public void setBalance(@NonNull BigFraction balance) {
        BigInteger num = balance.getNumerator();
        BigInteger den = balance.getDenominator();

        final long[] numbers = new long[]{num.longValue(), den.longValue()};
        put(BALANCE, numbers);
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

        return compareLhs.compareToIgnoreCase(compareRhs.toLowerCase());
    }
}
