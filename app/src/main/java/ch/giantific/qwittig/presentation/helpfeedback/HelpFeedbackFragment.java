/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHelpFeedbackBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.helpfeedback.di.HelpFeedbackComponent;

/**
 * Displays help and feedback items in a {@link RecyclerView} list.
 */
public class HelpFeedbackFragment extends BaseFragment<HelpFeedbackComponent, HelpFeedbackContract.Presenter, BaseFragment.ActivityListener<HelpFeedbackComponent>>
        implements HelpFeedbackContract.ViewListener {

    private static final int RC_INVITE = 0;
    private FragmentHelpFeedbackBinding binding;

    public HelpFeedbackFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHelpFeedbackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
        presenter.attachView(this);
    }

    private void setupRecyclerView() {
        binding.rvHelpFeedback.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvHelpFeedback.setHasFixedSize(true);
        final HelpFeedbackRecyclerAdapter adapter = new HelpFeedbackRecyclerAdapter(presenter);
        binding.rvHelpFeedback.setAdapter(adapter);
    }

    @Override
    protected void injectDependencies(@NonNull HelpFeedbackComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvHelpFeedback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_INVITE:
//                if (resultCode == Activity.RESULT_OK) {
//                    final String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
//                    showMessage(R.string.toast_recommend_sent, ids.length);
//                } else {
//                    showMessage(R.string.toast_recommend_failed);
//                }
//                break;
        }
    }

    @Override
    public void sendEmail(@NonNull String recipient, @StringRes int subject) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(subject));
        intent.putExtra(Intent.EXTRA_TEXT, getDeviceInfo());
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
        intent.putExtra(Intent.EXTRA_TEXT, String.format("%s%n%n%s", getDeviceInfo(), getString(body)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.toast_error_not_supported);
        }
    }

    private String getDeviceInfo() {
        return getString(R.string.email_device_build_info, Build.VERSION.RELEASE, Build.DEVICE,
                Build.MODEL, Build.PRODUCT, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME);
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
//                .build();
//        try {
//            startActivityForResult(intent, RC_INVITE);
//        } catch (ActivityNotFoundException e) {
//            showMessage(R.string.toast_error_not_supported);
//        }
    }
}
