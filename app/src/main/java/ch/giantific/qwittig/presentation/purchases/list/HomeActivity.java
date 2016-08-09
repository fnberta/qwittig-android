/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Base64;
import android.view.ActionMode;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.presentation.camera.CameraActivity;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsViewModel.PurchaseDetailsResult;
import ch.giantific.qwittig.presentation.purchases.list.di.DraftsListViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.di.PurchasesListViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsFragment;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesFragment;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesViewModel;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.Utils;
import timber.log.Timber;

/**
 * Provides the launcher activity for {@link Qwittig}, hosts a viewpager with
 * {@link PurchasesFragment} and {@link DraftsFragment} that display lists of recent
 * purchases and open drafts. Only loads the fragments if the  user is logged in.
 * <p>
 * Handles the case when a user is invited to a group and he/she wants to join it or declines the
 * invitation.
 * <p>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class HomeActivity extends BaseNavDrawerActivity<HomeSubcomponent> implements
        DraftsFragment.ActivityListener,
        HomeViewModel.ViewListener,
        JoinGroupDialogFragment.DialogInteractionListener {

    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    @Inject
    HomeViewModel mHomeViewModel;
    @Inject
    PurchasesViewModel mPurchasesViewModel;
    @Inject
    DraftsViewModel mDraftsViewModel;
    private DraftsFragment mDraftsFragment;
    private ActivityHomeBinding mBinding;
    private ProgressDialog mProgressDialog;
    private HomeTabsAdapter mTabsAdapter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case LocalBroadcast.DataType.OCR_PURCHASE_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                if (successful) {
                    final String ocrPurchaseId = intent.getStringExtra(LocalBroadcast.INTENT_EXTRA_OCR_PURCHASE_ID);
                    mHomeViewModel.onOcrPurchaseReady(ocrPurchaseId);
                } else {
                    mHomeViewModel.onOcrPurchaseFailed();
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_DrawStatusBar);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        mBinding.setViewModel(mHomeViewModel);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_home);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        setupGoogleApiClient();

        if (mUserLoggedIn) {
            if (mHomeViewModel.isDraftsAvailable()) {
                final DraftsFragment draftsFragment = savedInstanceState != null
                        ? (DraftsFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT)
                        : new DraftsFragment();
                setupTabs(draftsFragment);
            } else {
                setupTabs(null);
            }

            checkForInvitation();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        mComponent = navComp.plus(new HomeViewModelModule(savedInstanceState),
                new PurchasesListViewModelModule(savedInstanceState),
                new DraftsListViewModelModule(savedInstanceState));
        mComponent.inject(this);
        mHomeViewModel.attachView(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mHomeViewModel, mPurchasesViewModel, mDraftsViewModel});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUserLoggedIn && mHomeViewModel.isDraftsAvailable()) {
            getSupportFragmentManager().putFragment(outState, STATE_DRAFTS_FRAGMENT, mDraftsFragment);
        }
    }

    private void setupTabs(@Nullable DraftsFragment draftsFragment) {
        mTabsAdapter = new HomeTabsAdapter(getSupportFragmentManager());
        mTabsAdapter.addInitialFragment(new PurchasesFragment(), getString(R.string.tab_purchases));
        if (draftsFragment != null) {
            mDraftsFragment = draftsFragment;
            mTabsAdapter.addInitialFragment(mDraftsFragment, getString(R.string.tab_drafts));
        }
        mBinding.viewpager.setAdapter(mTabsAdapter);
        mBinding.tabs.setupWithViewPager(mBinding.viewpager);
    }

    private void toggleToolbarScrollFlags(boolean scroll) {
        final AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(scroll
                ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                : 0);
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Timber.w("GoogleApiClient onConnectionFailed: %s", connectionResult);
                    }
                })
                .addApi(AppInvite.API)
                .build();
    }

    private void checkForInvitation() {
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, false)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult result) {
                        if (result.getStatus().isSuccess()) {
                            final Intent intent = result.getInvitationIntent();
                            final String deepLink = AppInviteReferral.getDeepLink(intent);
                            Timber.d("deepLink %s", deepLink);
                            final Uri uri = Uri.parse(deepLink);

                            final String identityId = uri.getQueryParameter(GroupRepository.INVITATION_IDENTITY);
                            final String groupName = uri.getQueryParameter(GroupRepository.INVITATION_GROUP);
                            final String inviterNickname = uri.getQueryParameter(GroupRepository.INVITATION_INVITER);
                            mHomeViewModel.handleInvitation(identityId, groupName, inviterNickname);
                        } else {
                            Timber.i("getInvitation: no deep link found.");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHomeViewModel.onViewVisible();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mHomeViewModel.onViewGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_SAVED:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_SAVED_AUTO:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT:
                        showMessage(R.string.toast_purchase_added_draft);
                        break;
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT_CHANGES:
                        showMessage(R.string.toast_changes_saved_as_draft);
                        break;
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DRAFT_DELETED:
                        mDraftsViewModel.onDraftDeleted(data.getStringExtra(Navigator.INTENT_OBJECT_ID));
                        break;
                    case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsResult.PURCHASE_DELETED:
                        mPurchasesViewModel.onPurchaseDeleted(data.getStringExtra(Navigator.INTENT_OBJECT_ID));
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_PATH);
                        encodeReceipt(imagePath);
                        break;
                    case Activity.RESULT_CANCELED:
                        mHomeViewModel.onReceiptImageDiscarded();
                        break;
                    case CameraActivity.RESULT_ERROR:
                        mHomeViewModel.onReceiptImageFailed();
                }
                break;
        }
    }

    private void encodeReceipt(@NonNull final String receiptImagePath) {
        Glide.with(this)
                .load(receiptImagePath)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, PurchaseRepository.JPEG_COMPRESSION_RATE)
                .into(new SimpleTarget<byte[]>(PurchaseRepository.WIDTH, PurchaseRepository.HEIGHT) {
                    @Override
                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        final String base64 = Base64.encodeToString(resource, Base64.DEFAULT);
                        mHomeViewModel.onReceiptImageTaken(base64);
                        deleteReceiptFile(receiptImagePath);
                    }
                });
    }

    private void deleteReceiptFile(@NonNull String receiptImagePath) {
        final File origReceipt = new File(receiptImagePath);
        if (!origReceipt.delete()) {
            Timber.w("failed to delete receipt image file");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAPTURE_IMAGES:
                if (Utils.verifyPermissions(grantResults)) {
                    mNavigator.startCamera();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    mNavigator.startSystemSettings();
                                }
                            });
                }

                break;
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        setupTabs(mHomeViewModel.isDraftsAvailable() ? new DraftsFragment() : null);
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mDraftsFragment);
    }

    @Override
    public void toggleDraftTab(boolean draftsAvailable) {
        if (draftsAvailable) {
            if (mDraftsFragment == null) {
                toggleToolbarScrollFlags(true);
                mDraftsFragment = new DraftsFragment();
                mTabsAdapter.addFragment(mDraftsFragment, getString(R.string.tab_drafts));
            }
        } else if (mDraftsFragment != null) {
            toggleToolbarScrollFlags(false);
            mTabsAdapter.removeFragment(mDraftsFragment);
            mDraftsFragment = null;
        }
    }

    @Override
    public void onJoinInvitedGroupSelected(@NonNull String identityId) {
        mHomeViewModel.onJoinInvitedGroupSelected(identityId);
    }

    @Override
    public void onDiscardInvitationSelected() {
        mHomeViewModel.onDiscardInvitationSelected();
    }

    @Override
    public void showGroupJoinDialog(@NonNull String identityId,
                                    @NonNull String groupName,
                                    @NonNull String inviterNickname) {
        JoinGroupDialogFragment.display(getSupportFragmentManager(), identityId, groupName, inviterNickname);
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        mProgressDialog = ProgressDialog.show(this, null, getString(message), true);
    }

    @Override
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(this)) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            mNavigator.startCamera();
        }
    }

    private boolean permissionsAreGranted() {
        int hasCameraPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (hasCameraPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAPTURE_IMAGES);
            return false;
        }

        return true;
    }
}
