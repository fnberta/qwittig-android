/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.support.annotation.NonNull;

/**
 * Created by fabio on 10.01.16.
 */
public interface BaseWorkerListener {

    void onWorkerError(@NonNull String workerTag);
}
