/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.di.components.DaggerHomeComponent;
import ch.giantific.qwittig.di.components.HomeComponent;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.HomeDraftsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomeViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel.PurchaseResult;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesQueryMoreListener;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateListener;
import ch.giantific.qwittig.utils.ViewUtils;
import rx.Observable;

/**
 * Provides the launcher activity for {@link Qwittig}, hosts a viewpager with
 * {@link HomePurchasesFragment} and {@link HomeDraftsFragment} that display lists of recent
 * purchases and open drafts. Only loads the fragments if the  user is logged in.
 * <p/>
 * Handles the case when a user is invited to a group and he/she wants to join it or declines the
 * invitation.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class HomeActivity extends BaseNavDrawerActivity<HomeViewModel> implements
        HomeViewModel.ViewListener,
        HomePurchasesFragment.ActivityListener,
        HomeDraftsFragment.ActivityListener,
        PurchasesUpdateListener,
        PurchasesQueryMoreListener,
        GroupCreateDialogFragment.DialogInteractionListener {

    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final String STATE_PURCHASES_FRAGMENT = "STATE_PURCHASES_FRAGMENT";
    private static final String INVITED_GROUP_WORKER = "INVITED_GROUP_WORKER";
    private static final String URI_INVITED_GROUP_ID = "group";
    private static final String GROUP_JOIN_DIALOG = "GROUP_JOIN_DIALOG";
    private ActivityHomeBinding mBinding;
    private HomePurchasesViewModel mPurchasesViewModel;
    private HomeDraftsViewModel mDraftsViewModel;
    private HomePurchasesFragment mPurchasesFragment;
    private HomeDraftsFragment mDraftsFragment;
    private Group mInvitedGroup;
    private String mInviteInitiator;
    private String mInvitedGroupId;
    private ProgressDialog mProgressDialog;
    private int mInvitationAction;

    @Override
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.PURCHASES_UPDATED) {
            mPurchasesViewModel.updateList();
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
                //            checkForInvitations();
            } else {
                final FragmentManager fragmentManager = getFragmentManager();
                mPurchasesFragment = (HomePurchasesFragment)
                        fragmentManager.getFragment(savedInstanceState, STATE_PURCHASES_FRAGMENT);
                if (mViewModel.isDraftsAvailable()) {
                    mDraftsFragment = (HomeDraftsFragment)
                            fragmentManager.getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT);
                    setupTabs();
                }
            }
        }
    }

    private void injectViewModel(Bundle savedInstanceState) {
        final HomeComponent comp = DaggerHomeComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .homeViewModelModule(new HomeViewModelModule(savedInstanceState))
                .build();
        mViewModel = comp.getHomeViewModel();
        mBinding.setViewModel(mViewModel);
    }

    private void addFragments() {
        mPurchasesFragment = new HomePurchasesFragment();
        if (mViewModel.isDraftsAvailable()) {
            showViewPager();
        } else {
            showPurchasesFragment();
        }
    }

    private void showViewPager() {
        mDraftsFragment = new HomeDraftsFragment();
        setupTabs();
        toggleToolbarScrollFlags(true);
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
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
        getFragmentManager().beginTransaction()
                .add(R.id.container, mPurchasesFragment)
                .commit();
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
        final FragmentManager fragmentManager = getFragmentManager();
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
            final FragmentManager fragmentManager = getFragmentManager();
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

        mViewModel.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.detachView();
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
                    case PurchaseResult.PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                    case PurchaseResult.PURCHASE_ERROR:
                        showMessage(R.string.toast_create_image_file_failed);
                        break;
                }
                break;
        }
    }

    @Override
    public void setPurchasesViewModel(@NonNull HomePurchasesViewModel viewModel) {
        mPurchasesViewModel = viewModel;
    }

    @Override
    public void setDraftsViewModel(@NonNull HomeDraftsViewModel viewModel) {
        mDraftsViewModel = viewModel;
    }

    @Override
    void onLoginSuccessful() {
        super.onLoginSuccessful();

        // TODO: fix setLoading(true) because online query is still happening
        addFragments();
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        mPurchasesViewModel.onNewGroupSet();
        if (mViewModel.isDraftsAvailable()) {
            mDraftsViewModel.onNewGroupSet();
        }
    }

    @Override
    public void updateNavDrawerSelectedGroup() {
        mNavDrawerViewModel.notifySelectedGroupChanged();
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mDraftsFragment);
    }

    @Override
    public void showCreateGroupDialog(@StringRes int message) {

    }

    @Override
    public void onCreateGroupSelected() {
        final Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
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
    int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }

    @Override
    public boolean isNetworkAvailable() {
        return false;
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull String... args) {

    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {

    }

    @Override
    public void removeWorker(@NonNull String workerTag) {

    }
}

