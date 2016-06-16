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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.NavDrawerHeaderBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackActivity;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.login.LoginActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.DaggerNavDrawerComponent;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerViewModelModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsActivity;
import ch.giantific.qwittig.presentation.settings.general.SettingsViewModel;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileActivity;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import ch.giantific.qwittig.presentation.stats.StatsActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;

/**
 * Provides an abstract base class that sets up the navigation drawer and implements a couple of
 * default empty methods.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class BaseNavDrawerActivity<T extends ViewModel>
        extends BaseActivity<T>
        implements NavDrawerViewModel.ViewListener {

    private static final int NAV_DRAWER_ITEM_INVALID = -1;
    protected boolean mUserLoggedIn;
    @Inject
    protected NavDrawerViewModel mNavDrawerViewModel;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Menu mNavigationViewMenu;
    private ArrayAdapter mHeaderIdentitiesAdapter;
    private int mSelectedNavDrawerItem;

    @Override
    @CallSuper
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case LocalBroadcast.DataType.GROUP_UPDATED:
                mNavDrawerViewModel.onIdentitiesChanged();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final NavDrawerComponent navComp = DaggerNavDrawerComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navDrawerViewModelModule(new NavDrawerViewModelModule(savedInstanceState, this))
                .build();
        injectDependencies(navComp, savedInstanceState);

        mUserLoggedIn = mNavDrawerViewModel.isUserLoggedIn();
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

        mHeaderIdentitiesAdapter = new NavHeaderIdentitiesArrayAdapter(this, mNavDrawerViewModel);
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
        Intent intent;
        switch (itemId) {
            case R.id.nav_home:
                intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_finance:
                intent = new Intent(this, FinanceActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_tasks:
                intent = new Intent(this, TasksActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_stats:
                intent = new Intent(this, StatsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, INTENT_REQUEST_SETTINGS);
                break;
            case R.id.nav_help_feedback:
                intent = new Intent(this, HelpFeedbackActivity.class);
                startActivity(intent);
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
            case INTENT_REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    mNavDrawerViewModel.onLoginSuccessful();
                    onLoginSuccessful();
                } else {
                    finish();
                }
                break;
            case INTENT_REQUEST_SETTINGS:
                switch (resultCode) {
                    case SettingsViewModel.Result.LOGOUT:
                        mNavDrawerViewModel.onLogout();
                        break;
                    case SettingsViewModel.Result.GROUP_SELECTED:
                        mNavDrawerViewModel.onIdentityChanged();
                        break;
                    case SettingsViewModel.Result.GROUP_CHANGED:
                        mNavDrawerViewModel.onIdentitiesChanged();
                        break;
                }
                break;
            case INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        mNavDrawerViewModel.onProfileUpdated();
                        break;
                    case SettingsProfileViewModel.Result.CHANGES_DISCARDED:
                        showMessage(R.string.toast_changes_discarded);
                        break;
                }
                break;
        }
    }

    @CallSuper
    protected void onLoginSuccessful() {
        ParseQueryService.startUpdateAll(this);
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

    @Override
    public void notifyHeaderIdentitiesChanged() {
        mHeaderIdentitiesAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startProfileSettingsActivity() {
        final Intent intent = new Intent(this, SettingsProfileActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE,
                options.toBundle());
    }

    @Override
    public void startHomeActivityAndFinish() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void startLoginActivity() {
        final Intent intentLogin = new Intent(this, LoginActivity.class);
        intentLogin.setData(getIntent().getData());
//        Starting an activity with forResult and transitions during a lifecycle method results on
//        onActivityResult not being called
//        ActivityOptionsCompat activityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intentLogin, INTENT_REQUEST_LOGIN);
    }
}
