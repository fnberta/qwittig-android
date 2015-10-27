/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.os.Bundle;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.fragments.HelpFeedbackFragment;

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
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HelpFeedbackFragment())
                    .commit();
        }
    }
}
