/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.about.viewmodels.items.AboutItemViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.BaseAboutItemViewModel;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for a screen showing information about Qwittig.
 */
public interface AboutContract {

    interface Presenter extends BasePresenter<ViewListener> {

        BaseAboutItemViewModel[] getAboutItems();

        void onAboutItemClick(@NonNull AboutItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseView {
        // empty
    }
}
