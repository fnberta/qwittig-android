/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackItem;

/**
 * Created by fabio on 14.02.16.
 */
public interface HelpFeedbackViewModel extends ViewModel,
        HelpFeedbackRecyclerAdapter.AdapterInteractionListener {

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
    HelpFeedbackItem getItemAtPosition(int position);

    interface ViewListener extends ViewModel.ViewListener {
        void openWebsite(@NonNull String url);

        void sendEmail(@NonNull String recipient, @StringRes int subject);

        void sendEmail(@NonNull String recipient, @StringRes int subject, @StringRes int body);

        void openAppInPlayStore();
    }
}
