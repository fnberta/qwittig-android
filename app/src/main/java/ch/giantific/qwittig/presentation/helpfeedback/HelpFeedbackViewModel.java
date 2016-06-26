/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItem;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItemModel;

/**
 * Defines an observable view model for a screen showing a list of help and feedback items.
 */
public interface HelpFeedbackViewModel extends ViewModel<HelpFeedbackViewModel.ViewListener> {

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
    HelpFeedbackItemModel getItemAtPosition(int position);

    void onHelpFeedbackItemClicked(@NonNull HelpFeedbackItem itemModel);

    /**
     * Defines the interaction with attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void sendEmail(@NonNull String recipient, @StringRes int subject);

        void sendEmail(@NonNull String recipient, @StringRes int subject, @StringRes int body);

        void openAppInPlayStore();

        void startAppInvite();
    }
}
