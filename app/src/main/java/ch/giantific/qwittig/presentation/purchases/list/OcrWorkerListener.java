/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the action to take after the image has been ocr or after the process failed.
 */
public interface OcrWorkerListener extends BaseWorkerListener {
    /**
     * Sets the OCR processing stream.
     *
     * @param single    the {@link Single} that represents the ocr processing process
     * @param workerTag the tag of the worker
     */
    void setOcrStream(@NonNull Single<Void> single, @NonNull String workerTag);
}
