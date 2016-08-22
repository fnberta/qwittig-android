/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.jobs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.storage.UploadTask;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.jobs.di.DaggerJobsComponent;
import ch.giantific.qwittig.data.repositories.UserRepository;
import rx.SingleSubscriber;
import rx.Subscription;
import timber.log.Timber;

/**
 * Uploads a locally saved user to the online data store. Tries until it succeeds, even across
 * device reboots.
 */
public class UploadAvatarJob extends JobService {

    private static final String JOB_TAG = UploadAvatarJob.class.getCanonicalName();
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    private static final String KEY_AVATAR = "AVATAR";

    @Inject
    UserRepository userRepo;
    private Subscription subscription;

    public static boolean schedule(@NonNull FirebaseJobDispatcher jobDispatcher,
                                   @NonNull String avatar,
                                   @Nullable String identityId) {
        final Bundle extras = new Bundle();
        extras.putString(KEY_AVATAR, avatar);
        if (!TextUtils.isEmpty(identityId)) {
            extras.putString(KEY_IDENTITY_ID, identityId);
        }

        final Job job = jobDispatcher.newJobBuilder()
                .setService(UploadAvatarJob.class)
                .setTag(JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.NOW)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setReplaceCurrent(true)
                .setExtras(extras)
                .build();

        int result = jobDispatcher.schedule(job);
        return result == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS;
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        injectDependencies();

        final Bundle extras = jobParameters.getExtras();
        if (extras == null) {
            return false;
        }

        final String avatar = extras.getString(KEY_AVATAR, "");
        if (TextUtils.isEmpty(avatar)) {
            return false;
        }

        final String identityId = extras.getString(KEY_IDENTITY_ID);
        subscription = userRepo.uploadAvatar(avatar, identityId)
                .subscribe(new SingleSubscriber<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot value) {
                        Timber.i("avatar uploaded");
                        jobFinished(jobParameters, false);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to upload avatar with error:");
                        jobFinished(jobParameters, true);
                    }
                });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        subscription.unsubscribe();
        return true;
    }

    private void injectDependencies() {
        DaggerJobsComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }
}
