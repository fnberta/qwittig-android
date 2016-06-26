/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.about.itemmodels.AboutItem;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItemModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for a screen showing information about Qwittig.
 */
public interface AboutViewModel extends ViewModel<AboutViewModel.ViewListener> {

    /**
     * Returns the total number of items in the data set hold by the view model.
     *
     * @return the total number of items in this view model.
     */
    int getItemCount();

    /**
     * Returns the item at the position.
     *
     * @param position the position of the item
     * @return the item
     */
    AboutItemModel getItemAtPosition(int position);

    void onAboutItemClick(@NonNull AboutItem itemModel);

    /**
     * Defines the interaction with attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void openWebsite(@NonNull String url);
    }
}
