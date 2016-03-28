/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;

import ch.giantific.qwittig.domain.models.Group;

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
     * @param group           the group to get the role from
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

    /**
     * Returns whether the id as a valid Parse Object Id.
     *
     * @param id the id to check
     * @return whether the id as a valid Parse Object Id
     */
    public static boolean isObjectId(@NonNull String id) {
        return id.length() == 10;
    }
}
