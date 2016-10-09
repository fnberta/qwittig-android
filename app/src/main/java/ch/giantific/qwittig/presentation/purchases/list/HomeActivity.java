/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Base64;
import android.view.ActionMode;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.appinvite.AppInvite;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.presentation.camera.CameraContract.CameraResult;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.delegates.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract.PurchaseResult;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsContract.PurchaseDetailsResult;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsContract;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsFragment;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesContract;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesFragment;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
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
        HomeContract.ViewListener,
        JoinGroupDialogFragment.DialogInteractionListener,
        GoogleApiClientDelegate.GoogleInvitationCallback {

    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    @Inject
    HomeContract.Presenter homePresenter;
    @Inject
    HomeViewModel homeViewModel;
    @Inject
    PurchasesContract.Presenter purchasesPresenter;
    @Inject
    PurchasesViewModel purchasesViewModel;
    @Inject
    DraftsContract.Presenter draftsPresenter;
    @Inject
    DraftsViewModel draftsViewModel;
    @Inject
    GoogleApiClientDelegate googleApiDelegate;
    private NotificationManagerCompat notificationManager;
    private DraftsFragment draftsFragment;
    private ActivityHomeBinding binding;
    private HomeTabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_DrawStatusBar);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setPresenter(homePresenter);
        binding.setViewModel(homeViewModel);

        checkNavDrawerItem(R.id.nav_home);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        googleApiDelegate.createGoogleApiClient(AppInvite.API);
        notificationManager = NotificationManagerCompat.from(this);

        if (userLoggedIn) {
            if (homeViewModel.isDraftsAvailable()) {
                final DraftsFragment draftsFragment = savedInstanceState != null
                                                      ? (DraftsFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT)
                                                      : new DraftsFragment();
                setupTabs(draftsFragment);
            } else {
                setupTabs(null);
            }

            googleApiDelegate.checkForInvitation();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        component = navComp.plus(new PersistentViewModelsModule(savedInstanceState),
                new GoogleApiClientDelegateModule(this, null, this));
        component.inject(this);
        homePresenter.attachView(this);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{navPresenter, homePresenter, purchasesPresenter,
                draftsPresenter});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(HomeViewModel.TAG, homeViewModel);
        outState.putParcelable(PurchasesViewModel.TAG, purchasesViewModel);
        outState.putParcelable(DraftsViewModel.TAG, draftsViewModel);
        if (userLoggedIn && homeViewModel.isDraftsAvailable()) {
            getSupportFragmentManager().putFragment(outState, STATE_DRAFTS_FRAGMENT, draftsFragment);
        }
    }

    private void setupTabs(@Nullable DraftsFragment draftsFragment) {
        tabsAdapter = new HomeTabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(new PurchasesFragment(), getString(R.string.tab_purchases));
        if (draftsFragment != null) {
            this.draftsFragment = draftsFragment;
            tabsAdapter.addInitialFragment(this.draftsFragment, getString(R.string.tab_drafts));
        }
        binding.viewpager.setAdapter(tabsAdapter);
        binding.tabs.setupWithViewPager(binding.viewpager);
    }

    private void toggleToolbarScrollFlags(boolean scroll) {
        final AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(scroll
                              ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                                      AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                                      AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                              : 0);
    }

    @Override
    public void onDeepLinkFound(@NonNull Uri deepLink) {
        final String identityId = deepLink.getQueryParameter(GroupRepository.INVITATION_IDENTITY);
        final String groupName = deepLink.getQueryParameter(GroupRepository.INVITATION_GROUP);
        final String inviterNickname = deepLink.getQueryParameter(GroupRepository.INVITATION_INVITER);
        homePresenter.handleInvitation(identityId, groupName, inviterNickname);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.RC_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseResult.PURCHASE_SAVED:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseResult.PURCHASE_SAVED_AUTO:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseResult.PURCHASE_DRAFT:
                        showMessage(R.string.toast_purchase_added_draft);
                        break;
                    case PurchaseResult.PURCHASE_DRAFT_CHANGES:
                        showMessage(R.string.toast_changes_saved_as_draft);
                        break;
                    case PurchaseResult.PURCHASE_DRAFT_DELETED:
                        draftsPresenter.onDraftDeleted(data.getStringExtra(Navigator.EXTRA_GENERIC_STRING));
                        break;
                    case PurchaseResult.PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                }
                break;
            case Navigator.RC_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsResult.PURCHASE_DELETED:
                        purchasesPresenter.onPurchaseDeleted(data.getStringExtra(Navigator.EXTRA_GENERIC_STRING));
                        break;
                }
                break;
            case Navigator.RC_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(Navigator.EXTRA_GENERIC_STRING);
                        encodeReceipt(imagePath);
                        break;
                    case Activity.RESULT_CANCELED:
                        homePresenter.onReceiptImageDiscarded();
                        break;
                    case CameraResult.ERROR:
                        homePresenter.onReceiptImageFailed();
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
                        homePresenter.onReceiptImageTaken(base64);
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
                    navigator.startCamera();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    navigator.startSystemSettings();
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

        setupTabs(homeViewModel.isDraftsAvailable() ? new DraftsFragment() : null);
    }

    @Override
    public ActionMode startActionMode() {
        return toolbar.startActionMode(draftsFragment);
    }

    @Override
    public void toggleDraftTab(boolean draftsAvailable) {
        if (draftsAvailable) {
            if (draftsFragment == null) {
                toggleToolbarScrollFlags(true);
                draftsFragment = new DraftsFragment();
                tabsAdapter.addFragment(draftsFragment, getString(R.string.tab_drafts));
            }
        } else if (draftsFragment != null) {
            toggleToolbarScrollFlags(false);
            tabsAdapter.removeFragment(draftsFragment);
            draftsFragment = null;
        }
    }

    @Override
    public void clearOcrNotification(@NonNull String ocrPurchaseId) {
        notificationManager.cancel(ocrPurchaseId.hashCode());
    }

    @Override
    public void onJoinInvitedGroupSelected(@NonNull String identityId) {
        homePresenter.onJoinInvitedGroupSelected(identityId);
    }

    @Override
    public void onDiscardInvitationSelected() {
        homePresenter.onDiscardInvitationSelected();
    }

    @Override
    public void showGroupJoinDialog(@NonNull String identityId,
                                    @NonNull String groupName,
                                    @NonNull String inviterNickname) {
        JoinGroupDialogFragment.display(getSupportFragmentManager(), identityId, groupName, inviterNickname);
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(this)) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            navigator.startCamera();
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
