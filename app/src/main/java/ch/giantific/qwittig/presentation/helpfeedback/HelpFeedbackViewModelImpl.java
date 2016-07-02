/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackHeader;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItem;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItemModel;

/**
 * Provides an implementation of the {@link HelpFeedbackViewModel}.
 */
public class HelpFeedbackViewModelImpl extends ViewModelBaseImpl<HelpFeedbackViewModel.ViewListener>
        implements HelpFeedbackViewModel {

    private static final HelpFeedbackItemModel[] HELP_ITEMS = new HelpFeedbackItemModel[]{
            new HelpFeedbackHeader(R.string.header_help),
            new HelpFeedbackItem(R.string.help_faq, R.drawable.ic_help_black_24dp),
            new HelpFeedbackItem(R.string.help_contact_support, R.drawable.ic_email_black_24dp),
            new HelpFeedbackItem(R.string.help_tutorial, R.drawable.ic_school_black_24dp),
            new HelpFeedbackHeader(R.string.header_feedback),
            new HelpFeedbackItem(R.string.help_feedback, R.drawable.ic_bug_report_black_24dp),
            new HelpFeedbackItem(R.string.help_rate, R.drawable.ic_star_rate_black_24dp)
//            new HelpFeedbackItem(R.string.help_recommend, R.drawable.ic_favorite_black_24dp)
    };
    private static final String EMAIL_SUPPORT = "support@qwittig.ch";
    private static final String EMAIL_FEEDBACK = "feedback@qwittig.ch";
    private static final String FAQ_URL = "http://www.qwittig.ch/faq";

    private final Navigator mNavigator;

    public HelpFeedbackViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
    }

    @Override
    public HelpFeedbackItemModel getItemAtPosition(int position) {
        return HELP_ITEMS[position];
    }

    @Override
    public int getItemCount() {
        return HELP_ITEMS.length;
    }

    @Override
    public void onHelpFeedbackItemClicked(@NonNull HelpFeedbackItem itemModel) {
        final int titleId = itemModel.getTitle();
        switch (titleId) {
            case R.string.help_faq:
                mNavigator.openWebsite(FAQ_URL);
                break;
            case R.string.help_contact_support:
                mView.sendEmail(EMAIL_SUPPORT, R.string.email_support_subject, R.string.email_support_message);
                break;
            case R.string.help_tutorial:
                mNavigator.startFirstRun();
                break;
            case R.string.help_feedback:
                mView.sendEmail(EMAIL_FEEDBACK, R.string.email_feedback_subject);
                break;
            case R.string.help_rate:
                mView.openAppInPlayStore();
                break;
            case R.string.help_recommend:
                mView.startAppInvite();
                break;
        }
    }
}
