/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.save;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import rx.Observable;

/**
 * Saves a {@link Compensation} object to the online Parse.com database.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CompensationSaveWorker extends BaseWorker<Compensation, CompensationSaveListener> {

    public static final String WORKER_TAG = "COMPENSATION_SAVE_WORKER";
    private static final String LOG_TAG = CompensationSaveWorker.class.getSimpleName();
    @Inject
    CompensationRepository mCompsRepo;
    private Compensation mCompensation;

    /**
     * Constructs a new {@link CompensationSaveWorker} with a {@link Compensation} object as a
     * parameter.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link Compensation}. Because the fragment
     * is retained across configuration changes, there is no risk that the system will recreate it
     * with the default empty constructor.
     *
     * @param compensation the {@link Compensation} to save
     */
    @SuppressLint("ValidFragment")
    public CompensationSaveWorker(@NonNull Compensation compensation) {
        mCompensation = compensation;
    }

    public CompensationSaveWorker() {
        super();
    }

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<Compensation> getObservable(@NonNull Bundle args) {
        // TODO: mCompensation.setPaid(false) in error handling (if e != null)
        return mCompsRepo.saveCompensationAsync(mCompensation).toObservable();
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable, @NonNull String workerTag) {
        mActivity.setCompensationSaveStream(observable.toSingle(), workerTag);
    }
}
