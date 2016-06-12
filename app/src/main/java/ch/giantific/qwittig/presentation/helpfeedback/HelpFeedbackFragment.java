/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHelpFeedbackBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.helpfeedback.di.DaggerHelpFeedbackComponent;
import ch.giantific.qwittig.presentation.helpfeedback.di.HelpFeedbackViewModelModule;

/**
 * Displays help and feedback items in a {@link RecyclerView} list.
 */
public class HelpFeedbackFragment extends BaseRecyclerViewFragment<HelpFeedbackViewModel, BaseRecyclerViewFragment.ActivityListener>
        implements HelpFeedbackViewModel.ViewListener {

    private static final int RC_INVITE = 0;
    private FragmentHelpFeedbackBinding mBinding;

    public HelpFeedbackFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerHelpFeedbackComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .helpFeedbackViewModelModule(new HelpFeedbackViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHelpFeedbackBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvHelpFeedback;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new HelpFeedbackRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        // not relevant here
    }

    @Override
    public void openWebsite(@NonNull String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void sendEmail(@NonNull String recipient, @StringRes int subject) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(subject));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.toast_error_not_supported);
        }
    }

    @Override
    public void sendEmail(@NonNull String recipient,
                          @StringRes int subject,
                          @StringRes int body) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(body));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.toast_error_not_supported);
        }
    }

    @Override
    public void openAppInPlayStore() {
        final String appPackageName = getActivity().getPackageName();
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +
                    appPackageName));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
        }
    }

    @Override
    public void startAppInvite() {
        // TODO: customize email HTML message
//        final Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.recommend_title))
//                .setMessage(getString(R.string.recommend_message))
//                .setCallToActionText(getString(R.string.recommend_CTA))
        // TODO: add iOS
//                .setOtherPlatformsTargetApplication(PlatformMode.PROJECT_PLATFORM_IOS, getString(R.string.recommend_ios_id))
//                .build();
//        try {
//            startActivityForResult(intent, RC_INVITE);
//        } catch (ActivityNotFoundException e) {
//            showMessage(R.string.toast_error_not_supported);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        switch (requestCode) {
//            case RC_INVITE:
//                if (resultCode == Activity.RESULT_OK) {
//                    final String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
//                    showMessage(R.string.toast_recommend_sent, ids.length);
//                } else {
//                    showMessage(R.string.toast_recommend_failed);
//                }
//                break;
//        }
    }
}
