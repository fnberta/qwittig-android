/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.NavDrawerHeaderBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.navdrawer.di.DaggerNavDrawerComponent;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerViewModelModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsViewModel;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;

/**
 * Provides an abstract base class that sets up the navigation drawer and implements a couple of
 * default empty methods.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class BaseNavDrawerActivity<T> extends BaseActivity<T>
        implements NavDrawerViewModel.ViewListener {

    private static final int NAV_DRAWER_ITEM_INVALID = -1;
    protected boolean mUserLoggedIn;
    @Inject
    protected NavDrawerViewModel mNavDrawerViewModel;
    @Inject
    protected Navigator mNavigator;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Menu mNavigationViewMenu;
    private NavHeaderIdentitiesArrayAdapter mHeaderIdentitiesAdapter;
    private int mSelectedNavDrawerItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLoggedIn = mNavDrawerViewModel.isUserLoggedIn();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final NavDrawerComponent navComp = DaggerNavDrawerComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .navDrawerViewModelModule(new NavDrawerViewModelModule(savedInstanceState))
                .build();
        injectDependencies(navComp, savedInstanceState);
        mHeaderIdentitiesAdapter = new NavHeaderIdentitiesArrayAdapter(this, mNavDrawerViewModel);
        mNavDrawerViewModel.attachView(this);
        mNavDrawerViewModel.setSpinnerInteraction(mHeaderIdentitiesAdapter);
    }

    protected abstract void injectDependencies(@NonNull NavDrawerComponent navComp,
                                               @Nullable Bundle savedInstanceState);

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupNavDrawer();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mSelectedNavDrawerItem = menuItem.getItemId();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mNavigationViewMenu = navigationView.getMenu();

        final View navigationViewHeader = navigationView.getHeaderView(0);
        final NavDrawerHeaderBinding headerBinding = NavDrawerHeaderBinding.bind(navigationViewHeader);
        headerBinding.setViewModel(mNavDrawerViewModel);
        headerBinding.spDrawerGroup.setAdapter(mHeaderIdentitiesAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // override spinning arrow animation
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (mSelectedNavDrawerItem == getSelfNavDrawerItem()) {
                    return;
                }

                goToNavDrawerItem(mSelectedNavDrawerItem);
                mSelectedNavDrawerItem = 0;
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_INVALID;
    }

    private void goToNavDrawerItem(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                mNavigator.startHome();
                finish();
                break;
            case R.id.nav_finance:
                mNavigator.startFinance();
                finish();
                break;
            case R.id.nav_tasks:
                mNavigator.startTasks();
                finish();
                break;
            case R.id.nav_stats:
                mNavigator.startStats();
                finish();
                break;
            case R.id.nav_settings:
                mNavigator.startSettings();
                break;
            case R.id.nav_help_feedback:
                mNavigator.startHelpFeedback();
                break;
            case R.id.nav_about:
                mNavigator.startAbout();
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNavDrawerViewModel.onViewVisible();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mNavDrawerViewModel.onViewGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    setupScreenAfterLogin();
                    mUserLoggedIn = mNavDrawerViewModel.isUserLoggedIn();
                } else {
                    finish();
                }
                break;
            case Navigator.INTENT_REQUEST_SETTINGS:
                switch (resultCode) {
                    case SettingsViewModel.Result.LOGOUT:
                        mNavDrawerViewModel.afterLogout();
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        showMessage(R.string.toast_profile_update);
                        break;
                    case SettingsProfileViewModel.Result.CHANGES_DISCARDED:
                        showMessage(R.string.toast_changes_discarded);
                        break;
                }
                break;
        }
    }

    @CallSuper
    protected void setupScreenAfterLogin() {
        // empty default implementation
    }

    protected final void setStatusBarBackgroundColor(int color) {
        mDrawerLayout.setStatusBarBackgroundColor(color);
    }

    protected final void replaceDrawerIndicatorWithUp() {
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    protected final void unCheckNavDrawerItems() {
        mNavigationViewMenu.setGroupCheckable(R.id.nav_group_main, false, true);
    }

    protected final void checkNavDrawerItem(int itemId) {
        final MenuItem item = mNavigationViewMenu.findItem(itemId);
        item.setChecked(true);
    }
}
