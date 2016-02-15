/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.di.components.DaggerHomeComponent;
import ch.giantific.qwittig.di.components.HomeComponent;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel.PurchaseResult;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModel.PurchaseDetailsResult;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesQueryMoreWorkerListener;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesUpdateWorkerListener;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.utils.ViewUtils;
import rx.Observable;
import rx.Single;

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
        PurchasesUpdateWorkerListener,
        PurchasesQueryMoreWorkerListener,
        JoinGroupDialogFragment.DialogInteractionListener,
        JoinGroupWorkerListener {

    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final String STATE_PURCHASES_FRAGMENT = "STATE_PURCHASES_FRAGMENT";
    private static final String URI_INVITED_IDENTITY_ID = "id";
    private static final String URI_INVITED_GROUP_NAME = "group";
    private ActivityHomeBinding mBinding;
    private PurchasesViewModel mPurchasesViewModel;
    private DraftsViewModel mDraftsViewModel;
    private PurchasesFragment mPurchasesFragment;
    private DraftsFragment mDraftsFragment;
    private ProgressDialog mProgressDialog;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.PURCHASES_UPDATED) {
            mPurchasesViewModel.loadData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_home);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        mBinding.fabMenu.hideMenuButton(false);
        mBinding.fabMenu.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinding.fabMenu.showMenuButton(true);
            }
        }, ViewUtils.FAB_CIRCULAR_REVEAL_DELAY * 4);

        injectViewModel(savedInstanceState);

        if (mUserLoggedIn) {
            if (savedInstanceState == null) {
                addFragments();
                checkForInvitations();
            } else {
                final FragmentManager fragmentManager = getSupportFragmentManager();
                mPurchasesFragment = (PurchasesFragment)
                        fragmentManager.getFragment(savedInstanceState, STATE_PURCHASES_FRAGMENT);
                if (mViewModel.isDraftsAvailable()) {
                    mDraftsFragment = (DraftsFragment)
                            fragmentManager.getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT);
                    setupTabs();
                }
            }
        }
    }

    private void injectViewModel(@Nullable Bundle savedInstanceState) {
        final HomeComponent comp = DaggerHomeComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .homeViewModelModule(new HomeViewModelModule(savedInstanceState, this))
                .build();
        mViewModel = comp.getHomeViewModel();
        mBinding.setViewModel(mViewModel);
    }

    private void addFragments() {
        mPurchasesFragment = new PurchasesFragment();
        if (mViewModel.isDraftsAvailable()) {
            showViewPager();
        } else {
            showPurchasesFragment();
        }
    }

    private void showViewPager() {
        mDraftsFragment = new DraftsFragment();
        setupTabs();
        toggleToolbarScrollFlags(true);
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addFragment(mPurchasesFragment, getString(R.string.tab_purchases));
        tabsAdapter.addFragment(mDraftsFragment, getString(R.string.title_activity_purchase_drafts));
        mBinding.viewpager.setAdapter(tabsAdapter);
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

    private void showPurchasesFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mPurchasesFragment)
                .commit();
    }

    private void checkForInvitations() {
        final Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri uri = intent.getData();
            if (uri != null) {
                final String identityId = uri.getQueryParameter(URI_INVITED_IDENTITY_ID);
                final String groupName = uri.getQueryParameter(URI_INVITED_GROUP_NAME);
                mViewModel.handleInvitation(identityId, groupName);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final boolean draftsAvailable = mViewModel.isDraftsAvailable();
        if (draftsAvailable != mViewModel.updateDraftsAvailable()) {
            switchFragments();
        }
    }

    private void switchFragments() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (mViewModel.isDraftsAvailable()) {
            fragmentManager
                    .beginTransaction()
                    .remove(mPurchasesFragment)
                    .commit();
            fragmentManager.executePendingTransactions();
            showViewPager();
        } else {
            fragmentManager
                    .beginTransaction()
                    .remove(mPurchasesFragment)
                    .remove(mDraftsFragment)
                    .commit();
            fragmentManager.executePendingTransactions();
            showPurchasesFragment();
            toggleToolbarScrollFlags(false);
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
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_PURCHASE_MODIFY:
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
                        showMessage(R.string.toast_draft_deleted);
                        break;
                    case PurchaseResult.PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                    case PurchaseResult.PURCHASE_ERROR:
                        showMessage(R.string.toast_create_image_file_failed);
                        break;
                }
                break;
            case HomeActivity.INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsResult.PURCHASE_DELETED:
                        showMessage(R.string.toast_purchase_deleted);
                        break;
                    case PurchaseDetailsResult.GROUP_CHANGED:
                        mNavDrawerViewModel.notifySelectedGroupChanged();
                        break;
                }
                break;
        }
    }

    @Override
    public void showGroupJoinDialog(@NonNull String groupName) {
        JoinGroupDialogFragment.display(getSupportFragmentManager(), groupName);
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
    public void loadJoinGroupWorker(@NonNull String identityId) {
        JoinGroupWorker.attach(getSupportFragmentManager(), identityId);
    }

    @Override
    public void setJoinGroupStream(@NonNull Single<User> single, @NonNull String workerTag) {
        mViewModel.setJoinGroupStream(single, workerTag);
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
        ParseQueryService.startQueryAll(this);
        mNavDrawerViewModel.onIdentityChanged();
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
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        checkForInvitations();
        // TODO: fix setLoading(true) because online query is still happening
        addFragments();
    }

    @Override
    public void onIdentitySelected() {
        super.onIdentitySelected();

        mPurchasesViewModel.onIdentitySelected();
        if (mViewModel.isDraftsAvailable()) {
            mDraftsViewModel.onIdentitySelected();
        }
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mDraftsFragment);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseAddActivity(boolean autoMode) {
        final Intent intent = new Intent(this, PurchaseAddActivity.class);
        if (autoMode) {
            intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, true);
            startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY);
        } else {
            intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, false);
            final ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                    activityOptionsCompat.toBundle());
        }

        mBinding.fabMenu.close();
    }

    @Override
    public void setPurchasesUpdateStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mPurchasesViewModel.setPurchasesUpdateStream(observable, workerTag);
    }

    @Override
    public void setPurchasesQueryMoreStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mPurchasesViewModel.setPurchasesQueryMoreStream(observable, workerTag);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}

