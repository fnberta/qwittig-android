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
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackBaseItem;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackHeaderItem;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackItem;

/**
 * Provides an implementation of the {@link HelpFeedbackViewModel}.
 */
public class HelpFeedbackViewModelImpl extends ViewModelBaseImpl<HelpFeedbackViewModel.ViewListener>
        implements HelpFeedbackViewModel {

    private static final HelpFeedbackBaseItem[] HELP_ITEMS = new HelpFeedbackBaseItem[]{
            new HelpFeedbackHeaderItem(R.string.header_help),
            new HelpFeedbackItem(R.string.help_faq, R.drawable.ic_help_black_24dp),
            new HelpFeedbackItem(R.string.help_contact_support, R.drawable.ic_email_black_24dp),
            new HelpFeedbackItem(R.string.help_facebook, R.drawable.ic_facebook_box_black_24dp),
            new HelpFeedbackItem(R.string.help_twitter, R.drawable.ic_twitter_box_black_24dp),
            new HelpFeedbackHeaderItem(R.string.header_feedback),
            new HelpFeedbackItem(R.string.help_feedback, R.drawable.ic_bug_report_black_24dp),
            new HelpFeedbackItem(R.string.help_rate, R.drawable.ic_star_rate_black_24dp),
            new HelpFeedbackItem(R.string.help_recommend, R.drawable.ic_favorite_black_24dp)
    };
    private static final int HELP_TIPS = 1;
    private static final int HELP_CONTACT_SUPPORT = 2;
    private static final int HELP_FACEBOOK = 3;
    private static final int HELP_TWITTER = 4;
    private static final int HELP_GIVE_FEEDBACK = 6;
    private static final int HELP_RATE = 7;
    private static final int HELP_RECOMMEND = 8;
    private static final String EMAIL_SUPPORT = "support@qwittig.ch";
    private static final String EMAIL_FEEDBACK = "feedback@qwittig.ch";
    private static final String WEBSITE_URL = "http://www.qwittig.ch/faq";
    private static final String FACEBOOK_URL = "http://facebook.com/qwittig";
    private static final String TWITTER_URL = "http://twitter.com/qwittig";

    public HelpFeedbackViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull HelpFeedbackViewModel.ViewListener view,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepository) {
        super(savedState, view, eventBus, userRepository);
    }

    @Override
    public HelpFeedbackBaseItem getItemAtPosition(int position) {
        return HELP_ITEMS[position];
    }

    @Override
    public int getItemCount() {
        return HELP_ITEMS.length;
    }

    @Override
    public void onHelpFeedbackItemClicked(int position) {
        switch (position) {
            case HELP_TIPS:
                mView.openWebsite(WEBSITE_URL);
                break;
            case HELP_CONTACT_SUPPORT:
                mView.sendEmail(EMAIL_SUPPORT, R.string.email_support_subject, R.string.email_support_message);
                break;
            case HELP_FACEBOOK:
                mView.openWebsite(FACEBOOK_URL);
                break;
            case HELP_TWITTER:
                mView.openWebsite(TWITTER_URL);
                break;
            case HELP_GIVE_FEEDBACK:
                mView.sendEmail(EMAIL_FEEDBACK, R.string.email_feedback_subject);
                break;
            case HELP_RATE:
                mView.openAppInPlayStore();
                break;
            case HELP_RECOMMEND:
                mView.startAppInvite();
                break;
        }
    }
}
