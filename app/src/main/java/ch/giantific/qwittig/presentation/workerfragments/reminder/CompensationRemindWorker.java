/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import rx.Observable;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should either pay a compensation or
 * that he should accept an already paid one.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CompensationRemindWorker extends BaseWorker<String, CompensationReminderListener> {

    public static final String WORKER_TAG = "COMPENSATION_REMIND_WORKER";
    private static final String BUNDLE_COMPENSATION_ID = "BUNDLE_COMPENSATION_ID";
    private static final String LOG_TAG = CompensationRemindWorker.class.getSimpleName();
    @Inject
    ApiRepository mApiRepo;
    @Inject
    User mCurrentUser;
    private String mCompensationId;

    /**
     * Returns a new instance of {@link CompensationRemindWorker} with the compensation object id
     * as arguments.
     *
     * @param compensationId the object id of the compensation
     * @return a new instance of {@link CompensationRemindWorker}
     */
    @NonNull
    public static CompensationRemindWorker newInstance(@NonNull String compensationId) {
        CompensationRemindWorker fragment = new CompensationRemindWorker();
        Bundle args = new Bundle();
        args.putString(BUNDLE_COMPENSATION_ID, compensationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
        mCompensationId = args.getString(BUNDLE_COMPENSATION_ID, "");
        if (!TextUtils.isEmpty(mCompensationId)) {
            final Group currentGroup = mCurrentUser.getCurrentGroup();
            final String currencyCode = ParseUtils.getGroupCurrencyWithFallback(currentGroup);
            return mApiRepo.pushCompensationReminder(mCompensationId, currencyCode).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable, @NonNull String workerTag) {
        mActivity.setCompensationReminderStream(observable.toSingle(), mCompensationId, workerTag);
    }
}
