package ch.giantific.qwittig.ui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.HelpItem;
import ch.giantific.qwittig.ui.adapter.HelpFeedbackRecyclerAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class HelpFeedbackFragment extends Fragment implements
        HelpFeedbackRecyclerAdapter.AdapterInteractionListener {

    private static final HelpItem[] HELP_ITEMS = new HelpItem[]{
            new HelpItem(R.string.header_help),
            new HelpItem(R.string.help_faq, R.drawable.ic_help_black_24dp),
            new HelpItem(R.string.help_contact_support, R.drawable.ic_email_black_24dp),
            new HelpItem(R.string.help_facebook, R.drawable.ic_facebook_box_black_24dp),
            new HelpItem(R.string.help_twitter, R.drawable.ic_twitter_box_black_24dp),
            new HelpItem(R.string.header_feedback),
            new HelpItem(R.string.help_feedback, R.drawable.ic_bug_report_black_24dp),
            new HelpItem(R.string.help_rate, R.drawable.ic_star_rate_black_24dp),
            new HelpItem(R.string.help_recommend, R.drawable.ic_favorite_black_24dp)
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
    private RecyclerView mRecyclerView;

    public HelpFeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help_feedback, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_help_feedback);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HelpFeedbackRecyclerAdapter recyclerAdapter = new HelpFeedbackRecyclerAdapter(getActivity(), this,
                R.layout.row_help_feedback, HELP_ITEMS);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onHelpFeedbackItemClicked(int position) {
        switch (position) {
            case HELP_TIPS:
                openWebsite("http://www.qwittig.ch/faq");
                break;
            case HELP_CONTACT_SUPPORT:
                sendEmail(EMAIL_SUPPORT, getString(R.string.email_support_subject),
                        getString(R.string.email_support_message));
                break;
            case HELP_FACEBOOK:
                openWebsite("http://facebook.com/qwittig");
                break;
            case HELP_TWITTER:
                openWebsite("http://twitter.com/qwittig");
                break;
            case HELP_GIVE_FEEDBACK:
                sendEmail(EMAIL_FEEDBACK, getString(R.string.email_feedback_subject), "");
                break;
            case HELP_RATE:
                startPlayStore();
                break;
            case HELP_RECOMMEND:
                sendEmail("", getString(R.string.email_recommend_subject),
                        getString(R.string.email_recommend_message));
                break;
        }
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void sendEmail(String recipient, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(intent);
    }

    private void startPlayStore() {
        final String appPackageName = getActivity().getPackageName();
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +
                    appPackageName));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
        }
    }
}
