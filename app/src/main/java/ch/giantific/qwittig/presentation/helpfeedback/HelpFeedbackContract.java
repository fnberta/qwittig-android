/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.BaseHelpFeedbackItemViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.HelpFeedbackItemViewModel;

/**
 * Defines an observable view model for a screen showing a list of help and feedback items.
 */
public interface HelpFeedbackContract {

    interface Presenter extends BasePresenter<ViewListener> {

        BaseHelpFeedbackItemViewModel[] getHelpFeedbackItems();

        void onHelpFeedbackItemClicked(@NonNull HelpFeedbackItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseViewListener {
        void sendEmail(@NonNull String recipient, @StringRes int subject);

        void sendEmail(@NonNull String recipient, @StringRes int subject, @StringRes int body);

        void openAppInPlayStore();

        void startAppInvite();
    }
}
