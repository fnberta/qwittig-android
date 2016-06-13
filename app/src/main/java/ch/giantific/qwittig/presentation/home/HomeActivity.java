/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.home.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.home.di.HomeViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddPurchaseActivity;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModel.PurchaseDetailsResult;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesQueryMoreWorkerListener;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.Utils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import rx.Observable;
import rx.Single;
import timber.log.Timber;

/**
 * Provides the launcher activity for {@link Qwittig}, hosts a viewpager with
 * {@link PurchasesFragment} and {@link DraftsFragment} that display lists of recent
 * purchases and open drafts. Only loads the fragments if the  user is logged in.
 * <p/>
 * Handles the case when a user is invited to a group and he/she wants to join it or declines the
 * invitation.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class HomeActivity extends BaseNavDrawerActivity<HomeViewModel> implements
        HomeViewModel.ViewListener,
        PurchasesFragment.ActivityListener,
        DraftsFragment.ActivityListener,
        PurchasesQueryMoreWorkerListener,
        JoinGroupDialogFragment.DialogInteractionListener,
        JoinGroupWorkerListener,
        OcrWorkerListener {

    public static final String BRANCH_IS_INVITE = "+clicked_branch_link";
    public static final String BRANCH_IDENTITY_ID = "identityId";
    public static final String BRANCH_GROUP_NAME = "groupName";
    public static final String BRANCH_INVITER_NICKNAME = "inviterNickname";
    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final String STATE_PURCHASES_FRAGMENT = "STATE_PURCHASES_FRAGMENT";
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    private ActivityHomeBinding mBinding;
    private PurchasesViewModel mPurchasesViewModel;
    private DraftsViewModel mDraftsViewModel;
    private PurchasesFragment mPurchasesFragment;
    private DraftsFragment mDraftsFragment;
    private ProgressDialog mProgressDialog;
    private HomeTabsAdapter mTabsAdapter;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case LocalBroadcast.DataType.PURCHASES_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                mPurchasesViewModel.onDataUpdated(successful);
                break;
            }
            case LocalBroadcast.DataType.OCR_PURCHASE_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                if (successful) {
                    final String ocrPurchaseId = intent.getStringExtra(LocalBroadcast.INTENT_EXTRA_OCR_PURCHASE_ID);
                    mViewModel.onOcrPurchaseReady(ocrPurchaseId);
                } else {
                    mViewModel.onOcrPurchaseFailed();
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        mBinding.setViewModel(mViewModel);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_home);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        if (mUserLoggedIn) {
            mViewModel.onLoginSuccessful();

            if (savedInstanceState == null) {
                addFragments();
            } else {
                final FragmentManager fragmentManager = getSupportFragmentManager();
                mPurchasesFragment = (PurchasesFragment)
                        fragmentManager.getFragment(savedInstanceState, STATE_PURCHASES_FRAGMENT);
                if (mViewModel.isDraftsAvailable()) {
                    mDraftsFragment = (DraftsFragment)
                            fragmentManager.getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT);
                }
                setupTabs();
            }
        }
    }

    private void addFragments() {
        mPurchasesFragment = new PurchasesFragment();
        if (mViewModel.isDraftsAvailable()) {
            mDraftsFragment = new DraftsFragment();
        }
        setupTabs();
    }

    private void setupTabs() {
        mTabsAdapter = new HomeTabsAdapter(getSupportFragmentManager());
        mTabsAdapter.addInitialFragment(mPurchasesFragment, getString(R.string.tab_purchases));
        if (mViewModel.isDraftsAvailable()) {
            toggleToolbarScrollFlags(true);
            mTabsAdapter.addInitialFragment(mDraftsFragment, getString(R.string.title_activity_purchase_drafts));
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

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        final HomeSubcomponent component =
                navComp.plus(new HomeViewModelModule(savedInstanceState, this));
        component.inject(this);
        mViewModel = component.getHomeViewModel();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.onViewVisible();
        if (mUserLoggedIn) {
            checkBranchLink();
        }
    }

    private void checkBranchLink() {
        final Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error != null) {
                    Timber.e("deep link error, %s", error);
                    return;
                }

                final boolean openedWithInvite = referringParams.optBoolean(BRANCH_IS_INVITE, false);
                if (openedWithInvite) {
                    final String identityId = referringParams.optString(BRANCH_IDENTITY_ID);
                    final String groupName = referringParams.optString(BRANCH_GROUP_NAME);
                    final String inviterNickname = referringParams.optString(BRANCH_INVITER_NICKNAME);
                    mViewModel.handleInvitation(identityId, groupName, inviterNickname);
                }
            }
        }, getIntent().getData(), this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.onViewGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_SAVED:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_SAVED_AUTO:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DRAFT:
                        showMessage(R.string.toast_purchase_added_draft);
                        break;
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DRAFT_CHANGES:
                        showMessage(R.string.toast_changes_saved_as_draft);
                        break;
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DRAFT_DELETED:
                        showMessage(R.string.toast_draft_deleted);
                        break;
                    case AddEditPurchaseViewModel.PurchaseResult.PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                }
                break;
            case INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsResult.PURCHASE_DELETED:
                        showMessage(R.string.toast_purchase_deleted);
                        break;
                    case PurchaseDetailsResult.GROUP_CHANGED:
                        mNavDrawerViewModel.onIdentityChanged();
                        break;
                }
                break;
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_PATH);
                        encodeReceipt(imagePath);
                        break;
                    case Activity.RESULT_CANCELED:
                        mViewModel.onReceiptImageFailed();
                        break;
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
                        mViewModel.onReceiptImageTaken(resource);
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
                    getImage();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    startSystemSettings();
                                }
                            });
                }

                break;
        }
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        mViewModel.onLoginSuccessful();
        checkDrafts();
        addFragments();
    }

    @Override
    public void checkDrafts() {
        final boolean draftsAvailable = mViewModel.isDraftsAvailable();
        if (draftsAvailable != mViewModel.updateDraftsAvailable()) {
            toggleDraftTab();
        }
    }

    private void toggleDraftTab() {
        if (mViewModel.isDraftsAvailable()) {
            toggleToolbarScrollFlags(true);
            mDraftsFragment = new DraftsFragment();
            mTabsAdapter.addFragment(mDraftsFragment, getString(R.string.title_activity_purchase_drafts));
        } else {
            toggleToolbarScrollFlags(false);
            mTabsAdapter.removeFragment(mDraftsFragment);
        }
    }

    @Override
    public void onIdentitySelected() {
        super.onIdentitySelected();

        mPurchasesViewModel.onIdentitySelected();
        checkDrafts();
        if (mViewModel.isDraftsAvailable()) {
            mDraftsViewModel.onIdentitySelected();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mUserLoggedIn) {
            checkDrafts();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUserLoggedIn) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.putFragment(outState, STATE_PURCHASES_FRAGMENT, mPurchasesFragment);
            if (mViewModel.isDraftsAvailable()) {
                fragmentManager.putFragment(outState, STATE_DRAFTS_FRAGMENT, mDraftsFragment);
            }
        }
    }

    @Override
    public void onJoinInvitedGroupSelected() {
        mViewModel.onJoinInvitedGroupSelected();
    }

    @Override
    public void onDiscardInvitationSelected() {
        mViewModel.onDiscardInvitationSelected();
    }

    @Override
    public void setJoinGroupStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mViewModel.setJoinGroupStream(single, workerTag);
    }

    @Override
    public void setPurchasesViewModel(@NonNull PurchasesViewModel viewModel) {
        mPurchasesViewModel = viewModel;
    }

    @Override
    public void setDraftsViewModel(@NonNull DraftsViewModel viewModel) {
        mDraftsViewModel = viewModel;
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mDraftsFragment);
    }

    @Override
    public void showGroupJoinDialog(@NonNull String groupName, @NonNull String inviterNickname) {
        JoinGroupDialogFragment.display(getSupportFragmentManager(), groupName, inviterNickname);
    }

    @Override
    public void loadJoinGroupWorker(@NonNull String identityId) {
        JoinGroupWorker.attach(getSupportFragmentManager(), identityId);
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
    public void onGroupJoined() {
        ParseQueryService.startUpdateAll(this);
        mNavDrawerViewModel.onIdentitiesChanged();
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(this)) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            getImage();
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

    private void getImage() {
        final Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
    }

    private void startSystemSettings() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void loadOcrWorker(@NonNull byte[] receipt) {
        OcrWorker.attach(getSupportFragmentManager(), receipt);
    }

    @Override
    public void setOcrStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        mViewModel.setOcrStream(single, workerTag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseAddScreen(@Nullable String ocrPurchaseId) {
        final Intent intent = new Intent(this, AddPurchaseActivity.class);
        intent.putExtra(AddPurchaseActivity.INTENT_OCR_PURCHASE_ID, ocrPurchaseId);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    @Override
    public void setPurchasesQueryMoreStream
            (@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mPurchasesViewModel.setPurchasesQueryMoreStream(observable, workerTag);
    }
}

