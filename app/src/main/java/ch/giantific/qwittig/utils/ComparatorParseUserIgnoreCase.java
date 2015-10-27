/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseUser;

import java.util.Comparator;

import ch.giantific.qwittig.data.parse.models.User;

/**
 * Sorts differente {@link User} objects by their nickname, using case insensitive sorting.
 * <p/>
 * Implements {@link Comparator}.
 */
public class ComparatorParseUserIgnoreCase implements Comparator<ParseUser> {

    private static final String NOT_AVAILABLE = "n/a";

    public ComparatorParseUserIgnoreCase() {
        super();
    }

    @Override
    public int compare(@NonNull ParseUser lhs, @NonNull ParseUser rhs) {
        String nicknameLhs = ((User) lhs).getNickname();
        String nicknameRhs = ((User) rhs).getNickname();
        String compareLhs = NOT_AVAILABLE;
        String compareRhs = NOT_AVAILABLE;
        if (!TextUtils.isEmpty(nicknameLhs)) {
            compareLhs = nicknameLhs;
        }
        if (!TextUtils.isEmpty(nicknameRhs)) {
            compareRhs = nicknameRhs;
        }

        return compareLhs.compareToIgnoreCase(compareRhs.toLowerCase());
    }
}
