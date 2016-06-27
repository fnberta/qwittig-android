/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.di.DaggerHelpFeedbackComponent;
import ch.giantific.qwittig.presentation.helpfeedback.di.HelpFeedbackComponent;
import ch.giantific.qwittig.presentation.helpfeedback.di.HelpFeedbackViewModelModule;

/**
 * Hosts {@link HelpFeedbackFragment} that shows the user a list of help and feedback actions.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class HelpFeedbackActivity extends BaseActivity<HelpFeedbackComponent> {

    @Inject
    HelpFeedbackViewModel mHelpFeedbackViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new HelpFeedbackFragment())
                    .commit();
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerHelpFeedbackComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .helpFeedbackViewModelModule(new HelpFeedbackViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mHelpFeedbackViewModel});
    }
}
