package ch.giantific.qwittig.presentation.common.presenters;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Created by fabio on 28.09.16.
 */

public interface BasePresenter<T extends BaseView> extends BaseWorkerListener {

    void attachView(@NonNull T view);

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
