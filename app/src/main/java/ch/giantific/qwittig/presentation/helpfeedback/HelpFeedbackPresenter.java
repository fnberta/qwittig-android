/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.BaseHelpFeedbackItemViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.HelpFeedbackHeaderViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.HelpFeedbackItemViewModel;

/**
 * Provides an implementation of the {@link HelpFeedbackContract}.
 */
public class HelpFeedbackPresenter extends BasePresenterImpl<HelpFeedbackContract.ViewListener>
        implements HelpFeedbackContract.Presenter {

    private static final BaseHelpFeedbackItemViewModel[] HELP_ITEMS = new BaseHelpFeedbackItemViewModel[]{
            new HelpFeedbackHeaderViewModel(R.string.header_help),
            new HelpFeedbackItemViewModel(R.string.help_faq, R.drawable.ic_help_black_24dp),
            new HelpFeedbackItemViewModel(R.string.help_contact_support, R.drawable.ic_email_black_24dp),
            new HelpFeedbackItemViewModel(R.string.help_tutorial, R.drawable.ic_school_black_24dp),
            new HelpFeedbackHeaderViewModel(R.string.header_feedback),
            new HelpFeedbackItemViewModel(R.string.help_feedback, R.drawable.ic_bug_report_black_24dp),
            new HelpFeedbackItemViewModel(R.string.help_rate, R.drawable.ic_star_rate_black_24dp)
//            new HelpFeedbackItem(R.string.help_recommend, R.drawable.ic_favorite_black_24dp)
    };
    private static final String EMAIL_SUPPORT = "support@qwittig.ch";
    private static final String EMAIL_FEEDBACK = "feedback@qwittig.ch";
    private static final String FAQ_URL = "http://www.qwittig.ch/faq";

    @Inject
    public HelpFeedbackPresenter(@NonNull Navigator navigator,
                                 @NonNull UserRepository userRepo) {
        super(navigator, userRepo);
    }

    @Override
    public BaseHelpFeedbackItemViewModel[] getHelpFeedbackItems() {
        return HELP_ITEMS;
    }

    @Override
    public void onHelpFeedbackItemClicked(@NonNull HelpFeedbackItemViewModel itemViewModel) {
        final int titleId = itemViewModel.getTitle();
        switch (titleId) {
            case R.string.help_faq:
                navigator.openWebsite(FAQ_URL);
                break;
            case R.string.help_contact_support:
                view.sendEmail(EMAIL_SUPPORT, R.string.email_support_subject, R.string.email_support_message);
                break;
            case R.string.help_tutorial:
                navigator.startFirstRun();
                break;
            case R.string.help_feedback:
                view.sendEmail(EMAIL_FEEDBACK, R.string.email_feedback_subject);
                break;
            case R.string.help_rate:
                view.openAppInPlayStore();
                break;
            case R.string.help_recommend:
                view.startAppInvite();
                break;
        }
    }
}
