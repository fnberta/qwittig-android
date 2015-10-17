package ch.giantific.qwittig.ui.activities;

import android.os.Bundle;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.fragments.HelpFeedbackFragment;

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
