/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.services;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.services.di.DaggerServiceComponent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Uploads a locally saved user to the online data store. Tries until it succeeds, even across
 * device reboots.
 *
 * @see {@link GcmNetworkManager}
 */
public class SaveIdentityTaskService extends GcmTaskService {

    private static final String SERVICE_TASK = SaveIdentityTaskService.class.getCanonicalName();
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    @Inject
    UserRepository mUserRepo;

    public static void scheduleCurrentUser(@NonNull GcmNetworkManager networkManager) {
        final Bundle extras = new Bundle();
        startService(networkManager, extras);
    }

    public static void scheduleIdentity(@NonNull GcmNetworkManager networkManager,
                                        @NonNull String identityId) {
        final Bundle extras = new Bundle();
        extras.putString(KEY_IDENTITY_ID, identityId);
        startService(networkManager, extras);
    }

    private static void startService(@NonNull GcmNetworkManager networkManager,
                                     @NonNull Bundle extras) {
        final OneoffTask task = new OneoffTask.Builder()
                .setService(SaveIdentityTaskService.class)
                .setTag(SERVICE_TASK)
                .setExecutionWindow(0L, 5L)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setExtras(extras)
                .build();

        networkManager.schedule(task);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        injectDependencies();

        final Bundle extras = taskParams.getExtras();
        final String identityId = extras.getString(KEY_IDENTITY_ID, "");
        if (TextUtils.isEmpty(identityId)) {
            final User currentUser = mUserRepo.getCurrentUser();
            if (currentUser == null) {
                return GcmNetworkManager.RESULT_FAILURE;
            }

            final List<Identity> identities = currentUser.getIdentities();
            return mUserRepo.uploadIdentities(identities)
                    ? GcmNetworkManager.RESULT_SUCCESS
                    : GcmNetworkManager.RESULT_RESCHEDULE;
        }

        return mUserRepo.uploadIdentityId(identityId)
                ? GcmNetworkManager.RESULT_SUCCESS
                : GcmNetworkManager.RESULT_RESCHEDULE;
    }

    private void injectDependencies() {
        DaggerServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }
}
