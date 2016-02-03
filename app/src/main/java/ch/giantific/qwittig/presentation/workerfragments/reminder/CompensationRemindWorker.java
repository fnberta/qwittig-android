/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
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

    private static final String WORKER_TAG = CompensationRemindWorker.class.getCanonicalName();
    private static final String KEY_COMPENSATION_ID = "COMPENSATION_ID";
    @Inject
    ApiRepository mApiRepo;
    private String mCompensationId;

    /**
     * Attaches a new instance of {@link CompensationRemindWorker} with the compensation object id
     * as arguments.
     *
     * @param fm             the fragment manager to use for the transaction
     * @param compensationId the object id of the compensation
     * @return a new instance of {@link CompensationRemindWorker}
     */
    public static CompensationRemindWorker attach(@NonNull FragmentManager fm,
                                                  @NonNull String compensationId) {
        CompensationRemindWorker worker = (CompensationRemindWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = CompensationRemindWorker.newInstance(compensationId);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG + compensationId)
                    .commit();
        }

        return worker;
    }

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
        args.putString(KEY_COMPENSATION_ID, compensationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Override
    protected void onError() {
        // TODO: check tag
        mActivity.onWorkerError(WORKER_TAG + mCompensationId);
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
        mCompensationId = args.getString(KEY_COMPENSATION_ID, "");
        final User currentUser = mUserRepo.getCurrentUser();
        if (!TextUtils.isEmpty(mCompensationId) && currentUser != null) {
            final Group currentGroup = currentUser.getCurrentGroup();
            final String currencyCode = ParseUtils.getGroupCurrencyWithFallback(currentGroup);
            return mApiRepo.pushCompensationReminder(mCompensationId, currencyCode).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable) {
        mActivity.setCompensationRemindStream(observable.toSingle(), mCompensationId, WORKER_TAG + mCompensationId);
    }
}
