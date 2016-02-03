/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.save;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
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

    private static final String WORKER_TAG = CompensationSaveWorker.class.getCanonicalName();
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
    private CompensationSaveWorker(@NonNull Compensation compensation) {
        mCompensation = compensation;
    }

    /**
     * Attaches a new instance of a {@link CompensationSaveWorker}.
     *
     * @param fm           the fragment manager to use for the transaction.
     * @param compensation the compensation to save
     * @return a new instance of a {@link CompensationSaveWorker}
     */
    public static CompensationSaveWorker attach(@NonNull FragmentManager fm,
                                                @NonNull Compensation compensation) {
        CompensationSaveWorker worker = (CompensationSaveWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new CompensationSaveWorker(compensation);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
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
