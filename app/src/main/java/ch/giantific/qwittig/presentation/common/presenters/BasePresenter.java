package ch.giantific.qwittig.presentation.common.presenters;

import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Created by fabio on 28.09.16.
 */

public interface BasePresenter<T extends BaseViewListener> extends BaseWorkerListener {

    void attachView(@NonNull T view);

    /**
     * Saves the state of the view model in a bundle before recreation.
     *
     * @param outState the bundle to save the state in
     */
    void saveState(@NonNull Bundle outState);

    /**
     * Sets up RxJava composite subscriptions and loads the data for the view.
     */
    void onViewVisible();

    /**
     * Cleans up any long living tasks, e.g. RxJava subscriptions, in order to allow the view model
     * and the view it references to be garbage collected.
     */
    void onViewGone();
}
