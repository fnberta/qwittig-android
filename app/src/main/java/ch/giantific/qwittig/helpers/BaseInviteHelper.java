/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

/**
 * Provides an abstract base class for helper fragments that deal with the task of inviting users
 * to a group.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public abstract class BaseInviteHelper extends BaseHelper {

    private static final String LOG_TAG = BaseInviteHelper.class.getSimpleName();

    public BaseInviteHelper() {
        // empty default constructor
    }

    final void inviteUsers(@NonNull List<String> emails, @NonNull String groupName) {
        Map<String, Object> params = new HashMap<>();
        params.put(CloudCode.PARAM_EMAIL, emails);
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);
        ParseCloud.callFunctionInBackground(CloudCode.INVITE_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                if (e != null) {
                    onInviteUsersFailed(e);
                    return;
                }

                onUsersInvited();
            }
        });
    }

    protected abstract void onInviteUsersFailed(ParseException e);

    protected abstract void onUsersInvited();
}
