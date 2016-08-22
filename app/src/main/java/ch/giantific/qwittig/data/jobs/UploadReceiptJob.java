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
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import rx.SingleSubscriber;
import rx.Subscription;
import timber.log.Timber;

/**
 * Uploads a locally saved purchase to the online data store. Tries until it succeeds, even across
 * device reboots.
 */
public class UploadReceiptJob extends JobService {

    private static final String JOB_TAG = UploadReceiptJob.class.getCanonicalName();
    private static final String KEY_PURCHASE_ID = "PURCHASE_ID";
    private static final String KEY_RECEIPT = "RECEIPT";
    private static final String KEY_BUYER_ID = "BUYER_ID";

    @Inject
    PurchaseRepository purchaseRepo;
    private Subscription subscription;

    public static boolean schedule(@NonNull FirebaseJobDispatcher jobDispatcher,
                                   @NonNull String purchaseId,
                                   @Nullable String buyerId,
                                   @NonNull String receipt) {
        final Bundle extras = new Bundle();
        extras.putString(KEY_PURCHASE_ID, purchaseId);
        extras.putString(KEY_RECEIPT, receipt);
        extras.putString(KEY_BUYER_ID, buyerId);

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

        final String purchaseId = extras.getString(KEY_PURCHASE_ID, "");
        final String receipt = extras.getString(KEY_RECEIPT, "");
        final String buyerId = extras.getString(KEY_BUYER_ID, "");
        if (TextUtils.isEmpty(purchaseId) || TextUtils.isEmpty(receipt)) {
            return false;
        }

        subscription = purchaseRepo.uploadReceipt(purchaseId, buyerId, receipt)
                .subscribe(new SingleSubscriber<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot value) {
                        Timber.d("receipt uploaded");
                        jobFinished(jobParameters, false);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to upload receipt with error:");
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
