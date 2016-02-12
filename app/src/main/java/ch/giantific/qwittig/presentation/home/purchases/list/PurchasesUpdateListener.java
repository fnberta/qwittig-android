/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after purchases are updated.
 */
public interface PurchasesUpdateListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the purchases update stream
     *
     * @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setPurchasesUpdateStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag);
}
