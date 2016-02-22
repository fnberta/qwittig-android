/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Fetches the newest currency exchange rates online using {@link ExchangeRates}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class RatesWorker extends BaseWorker<Float, RatesWorkerListener> {

    private static final String WORKER_TAG = RatesWorker.class.getCanonicalName();
    private static final String KEY_BASE_CURRENCY = "BASE_CURRENCY";
    private static final String KEY_CURRENCY = "CURRENCY";
    @Inject
    PurchaseRepository mPurchaseRepo;

    public RatesWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link RatesWorker} with a base currency code as an argument.
     *
     * @param fm           the fragment manager to use for the transaction
     * @param baseCurrency the currency to use as a base for the foreign currencies
     * @return a new instance of {@link RatesWorker}
     */
    public static RatesWorker attach(@NonNull FragmentManager fm, @NonNull String baseCurrency,
                                     @NonNull String currency) {
        RatesWorker worker = (RatesWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new RatesWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_BASE_CURRENCY, baseCurrency);
            args.putString(KEY_CURRENCY, currency);
            worker.setArguments(args);

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

    @Nullable
    @Override
    protected Observable<Float> getObservable(@NonNull Bundle args) {
        final String baseCurrency = args.getString(KEY_BASE_CURRENCY, "");
        final String currency = args.getString(KEY_CURRENCY, "");
        if (!TextUtils.isEmpty(baseCurrency) && !TextUtils.isEmpty(currency)) {
            return mPurchaseRepo.getExchangeRate(baseCurrency, currency).toObservable();
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Float> observable) {
        mActivity.setRateFetchStream(observable.toSingle(), WORKER_TAG);
    }
}
