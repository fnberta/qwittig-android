/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseConfig;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.presentation.settings.addgroup.Currency;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides useful static utility methods related to the Parse.com framework.
 */
public class ParseUtils {

    private ParseUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a default {@link ParseACL} with read/write access for the role of the passed group.
     *
     * @param group the group to get the role from
     * @param roleWriteAccess whether to give the group role write access
     * @return a default {@link ParseACL}
     */
    @NonNull
    public static ParseACL getDefaultAcl(@NonNull Group group, boolean roleWriteAccess) {
        final String roleName = getGroupRoleName(group);
        final ParseACL acl = new ParseACL();
        acl.setRoleReadAccess(roleName, true);
        acl.setRoleWriteAccess(roleName, roleWriteAccess);
        return acl;
    }

    @NonNull
    private static String getGroupRoleName(@NonNull Group group) {
        return Group.ROLE_PREFIX + group.getObjectId();
    }
}
