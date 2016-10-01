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
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerPresenterModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsContract;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileContract;

/**
 * Provides an abstract base class that sets up the navigation drawer and implements a couple of
 * default empty methods.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class BaseNavDrawerActivity<T> extends BaseActivity<T>
        implements NavDrawerContract.ViewListener {

    private static final int NAV_DRAWER_ITEM_INVALID = -1;

    protected boolean userLoggedIn;
    @Inject
    protected Navigator navigator;
    @Inject
    NavDrawerContract.Presenter navPresenter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Menu navigationViewMenu;
    private NavHeaderIdentitiesArrayAdapter headerIdentitiesAdapter;
    private int selectedNavDrawerItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLoggedIn = navPresenter.isUserLoggedIn();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final NavDrawerComponent navComp = DaggerNavDrawerComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .navDrawerPresenterModule(new NavDrawerPresenterModule(savedInstanceState))
                .build();
        injectDependencies(navComp, savedInstanceState);
        headerIdentitiesAdapter = new NavHeaderIdentitiesArrayAdapter(this, navPresenter);
        navPresenter.attachView(this);
        navPresenter.setSpinnerInteraction(headerIdentitiesAdapter);
    }

    protected abstract void injectDependencies(@NonNull NavDrawerComponent navComp,
                                               @Nullable Bundle savedInstanceState);

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupNavDrawer();
    }

    private void setupNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            selectedNavDrawerItem = menuItem.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        navigationViewMenu = navigationView.getMenu();

        final View navigationViewHeader = navigationView.getHeaderView(0);
        final NavDrawerHeaderBinding headerBinding = NavDrawerHeaderBinding.bind(navigationViewHeader);
        headerBinding.setPresenter(navPresenter);
        headerBinding.setViewModel(navPresenter.getViewModel());
        headerBinding.spDrawerGroup.setAdapter(headerIdentitiesAdapter);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // override spinning arrow animation
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (selectedNavDrawerItem == getSelfNavDrawerItem()) {
                    return;
                }

                goToNavDrawerItem(selectedNavDrawerItem);
                selectedNavDrawerItem = 0;
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
    }

    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_INVALID;
    }

    private void goToNavDrawerItem(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                navigator.startHome();
                finish();
                break;
            case R.id.nav_finance:
                navigator.startFinance();
                finish();
                break;
            case R.id.nav_assignments:
                navigator.startAssignments();
                finish();
                break;
            case R.id.nav_stats:
                navigator.startStats();
                finish();
                break;
            case R.id.nav_settings:
                navigator.startSettings();
                break;
            case R.id.nav_help_feedback:
                navigator.startHelpFeedback();
                break;
            case R.id.nav_about:
                navigator.startAbout();
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        navPresenter.onViewVisible();
    }

    @Override
    protected void onStop() {
        super.onStop();

        navPresenter.onViewGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.RC_LOGIN:
                if (resultCode == RESULT_OK) {
                    setupScreenAfterLogin();
                    userLoggedIn = navPresenter.isUserLoggedIn();
                } else {
                    finish();
                }
                break;
            case Navigator.RC_SETTINGS:
                switch (resultCode) {
                    case SettingsContract.Result.LOGOUT:
                        navPresenter.afterLogout();
                        break;
                }
                break;
            case Navigator.RC_SETTINGS_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        showMessage(R.string.toast_profile_update);
                        break;
                    case SettingsProfileContract.Result.CHANGES_DISCARDED:
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
        drawerLayout.setStatusBarBackgroundColor(color);
    }

    protected final void replaceDrawerIndicatorWithUp() {
        drawerToggle.setDrawerIndicatorEnabled(false);
    }

    protected final void unCheckNavDrawerItems() {
        navigationViewMenu.setGroupCheckable(R.id.nav_group_main, false, true);
    }

    protected final void checkNavDrawerItem(int itemId) {
        final MenuItem item = navigationViewMenu.findItem(itemId);
        item.setChecked(true);
    }
}
