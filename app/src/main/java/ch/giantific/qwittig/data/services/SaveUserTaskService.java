/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.services;

import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.services.di.DaggerServiceComponent;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Uploads a locally saved user to the online data store. Tries until it succeeds, even across
 * device reboots.
 *
 * @see {@link GcmNetworkManager}
 */
public class SaveUserTaskService extends GcmTaskService {

    private static final String SERVICE_TASK = SaveUserTaskService.class.getCanonicalName();
    @Inject
    UserRepository mUserRepo;

    public static void schedule(@NonNull GcmNetworkManager networkManager) {
        final OneoffTask task = new OneoffTask.Builder()
                .setService(SaveUserTaskService.class)
                .setTag(SERVICE_TASK)
                .setExecutionWindow(0L, 5L)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .build();

        networkManager.schedule(task);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        injectDependencies();

        return mUserRepo.uploadCurrentUserProfile()
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
