package ch.giantific.qwittig.utils;

import android.text.TextUtils;

import java.util.Comparator;

import ch.giantific.qwittig.data.parse.models.User;

/**
* Created by fabio on 24.02.15.
*/
public class ComparatorParseUserIgnoreCase implements Comparator<Object> {

    private static final String NOT_AVAILABLE = "n/a";

    public ComparatorParseUserIgnoreCase() {
        super();
    }

    @Override
    public int compare(Object lhs, Object rhs) {
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
