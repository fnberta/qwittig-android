/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.save;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import rx.Observable;

/**
 * Saves a {@link Compensation} object to the online Parse.com database.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CompensationSaveWorker extends BaseWorker<Compensation, CompensationSaveWorkerListener> {

    public static final String WORKER_TAG = "COMPENSATION_SAVE_WORKER";
    @Inject
    CompensationRepository mCompsRepo;
    private Compensation mCompensation;

    public CompensationSaveWorker() {
        // required empty constructor
    }

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

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Compensation> getObservable(@NonNull Bundle args) {
        return mCompsRepo.saveCompensationAsync(mCompensation).toObservable();
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable) {
        mActivity.setCompensationSaveStream(observable.toSingle(), WORKER_TAG);
    }
}
