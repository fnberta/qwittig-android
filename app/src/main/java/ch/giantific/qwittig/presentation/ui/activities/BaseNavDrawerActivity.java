/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.NavdrawerHeaderBinding;
import ch.giantific.qwittig.di.components.DaggerNavDrawerComponent;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.presentation.ui.adapters.NavHeaderGroupsArrayAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.SettingsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.SettingsProfileFragment;
import ch.giantific.qwittig.presentation.viewmodels.NavDrawerViewModel;
import ch.giantific.qwittig.presentation.viewmodels.ViewModel;
import ch.giantific.qwittig.services.ParseQueryService;

/**
 * Provides an abstract base class that sets up the navigation drawer and implements a couple of
 * default empty methods.
 * <p/>
 * Also initialises the in-app billing framework.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class BaseNavDrawerActivity<T extends ViewModel>
        extends BaseActivity<T>
        implements NavDrawerViewModel.ViewListener {

    static final String URI_INVITED_EMAIL = "email";
    private static final int NAVDRAWER_ITEM_INVALID = -1;

    @Inject
    NavDrawerViewModel mNavDrawerViewModel;
    private NavdrawerHeaderBinding mHeaderBinding;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Menu mNavigationViewMenu;
    private View mNavigationViewHeader;
    private ArrayAdapter mHeaderGroupsAdapter;
    private int mSelectedNavDrawerItem;

    @Override
    @CallSuper
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case LocalBroadcastImpl.DATA_TYPE_GROUP_UPDATED:
                mNavDrawerViewModel.onGroupChanged();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final NavDrawerComponent navComp = DaggerNavDrawerComponent.create();
        injectNavDrawerDependencies(navComp);
    }

    protected abstract void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp);

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupNavDrawer();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navdrawer_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mSelectedNavDrawerItem = menuItem.getItemId();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mNavigationViewMenu = navigationView.getMenu();
        mNavigationViewHeader = navigationView.getHeaderView(0);

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

        mDrawerLayout.setDrawerListener(mDrawerToggle);
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

        mNavDrawerViewModel.attachView(this);
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
                    case SettingsFragment.RESULT_LOGOUT:
                        mNavDrawerViewModel.onLogout();
                        break;
                    case SettingsFragment.RESULT_GROUP_CHANGED:
                        mNavDrawerViewModel.onGroupChanged();
                        break;
                }
                break;
            case INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        mNavDrawerViewModel.onProfileUpdated();
                        break;
                    case SettingsProfileFragment.RESULT_CHANGES_DISCARDED:
                        Snackbar.make(mHeaderBinding.spDrawerGroup, R.string.toast_changes_discarded,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    @CallSuper
    void onLoginSuccessful() {
        // TODO: check whether user is in group?
        ParseQueryService.startQueryAll(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mNavDrawerViewModel.detachView();
    }

    final boolean isUserLoggedIn() {
        if (mNavDrawerViewModel.isUserLoggedIn()) {
            return true;
        }

        startLoginActivity();
        return false;
    }

    private void startLoginActivity() {
        Intent intentLogin = new Intent(this, LoginActivity.class);
        Uri uri = getIntent().getData();
        if (uri != null) {
            String invitedEmail = uri.getQueryParameter(URI_INVITED_EMAIL);
            intentLogin.putExtra(LoginActivity.INTENT_URI_EMAIL, invitedEmail);
        }

//        Starting an activity with forResult and transitions during a lifecycle method results on
//        onActivityResult not being called
//        ActivityOptionsCompat activityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intentLogin, INTENT_REQUEST_LOGIN);
    }

    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    final void setStatusBarBackgroundColor(int color) {
        mDrawerLayout.setStatusBarBackgroundColor(color);
    }

    final void replaceDrawerIndicatorWithUp() {
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    final void unCheckNavDrawerItems() {
        mNavigationViewMenu.setGroupCheckable(R.id.nav_group_main, false, true);
    }

    final void checkNavDrawerItem(int itemId) {
        final MenuItem item = mNavigationViewMenu.findItem(itemId);
        item.setChecked(true);
    }

    @Override
    public void bindHeaderView() {
        mHeaderBinding = NavdrawerHeaderBinding.bind(mNavigationViewHeader);
        mHeaderBinding.setViewModel(mNavDrawerViewModel);
    }

    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(mToolbar, resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setupHeaderGroupSelection(@NonNull List<Group> groups) {
        mHeaderGroupsAdapter = new NavHeaderGroupsArrayAdapter(this, groups);
        mHeaderBinding.spDrawerGroup.setAdapter(mHeaderGroupsAdapter);
    }

    @Override
    public void notifyHeaderGroupListChanged() {
        mHeaderGroupsAdapter.notifyDataSetChanged();
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
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNewGroupSet() {
        mViewModel.onNewGroupSet();
    }
}
