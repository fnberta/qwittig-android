/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.os.Bundle;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;

/**
 * Hosts {@link HelpFeedbackFragment} that shows the user a list of help and feedback actions.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class HelpFeedbackActivity extends BaseActivity {

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
}
