/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.save;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Single;

/**
 * Defines the action to take after the purchases are saved and pinned to the local data store
 * or after the save process failed.
 */
public interface PurchaseSaveWorkerListener extends BaseWorkerListener {
    /**
     * Sets the purchase save rx.single.
     */
    void setPurchaseSaveStream(@NonNull Single<Purchase> single, @NonNull String workerTag);
}
