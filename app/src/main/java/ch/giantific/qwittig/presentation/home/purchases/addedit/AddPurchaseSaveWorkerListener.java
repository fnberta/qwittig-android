/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the action to take after the purchases are saved and pinned to the local data store
 * or after the save process failed.
 */
public interface AddPurchaseSaveWorkerListener extends BaseWorkerListener {

    /**
     * Sets the save purchase stream.
     *
     * @param single    the {@link Single} that emits the save result
     * @param workerTag the tag of the worker fragment
     */
    void setPurchaseSaveStream(@NonNull Single<Purchase> single, @NonNull String workerTag);
}
