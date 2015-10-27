/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseConfig;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.BlurTransformation;
import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.services.ParseQueryService;
import ch.giantific.qwittig.ui.adapters.NavHeaderGroupsArrayAdapter;
import ch.giantific.qwittig.ui.fragments.SettingsFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.inappbilling.IabHelper;
import ch.giantific.qwittig.utils.inappbilling.IabKey;
import ch.giantific.qwittig.utils.inappbilling.IabResult;
import ch.giantific.qwittig.utils.inappbilling.Inventory;
import ch.giantific.qwittig.utils.inappbilling.Purchase;

/**
 * Provides an abstract base class that sets up the navigation drawer and implements a couple of
 * default empty methods.
 * <p/>
 * Also initialises the in-app billing framework.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class BaseNavDrawerActivity extends BaseActivity implements
        LocalQuery.ObjectLocalFetchListener {

    static final String URI_INVITED_EMAIL = "URI_INVITED_EMAIL";
    private static final int NAVDRAWER_ITEM_INVALID = -1;
    private static final String SKU_PREMIUM = "ch.giantific.qwittig.iab.premium";
    private static final int RC_REQUEST = 10001;
    private static final String LOG_TAG = BaseNavDrawerActivity.class.getSimpleName();

    User mCurrentUser;
    Group mCurrentGroup;
    boolean mUserIsLoggedIn;
    @Nullable
    IabHelper mIabHelper;
    boolean mIsPremium;
    boolean mInTrialMode;
    private DrawerLayout mDrawerLayout;
    private Menu mNavigationViewMenu;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedNavDrawerItem;
    private int mFetchCounter = 0;
    private int mGroupsCount;
    private TextView mTextViewHeaderNickname;
    private ImageView mImageViewHeader;
    private ImageView mImageViewHeaderAvatar;
    private Spinner mSpinnerGroups;
    private NavHeaderGroupsArrayAdapter mSpinnerGroupsAdapter;
    @NonNull
    private List<ParseObject> mGroups = new ArrayList<>();

    @NonNull
    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            int dataType = intent.getIntExtra(ParseQueryService.INTENT_DATA_TYPE, 0);
            switch (dataType) {
                case ParseQueryService.DATA_TYPE_PURCHASE:
                    onPurchasesPinned();
                    break;
                case ParseQueryService.DATA_TYPE_USER:
                    onUsersPinned();
                    break;
                case ParseQueryService.DATA_TYPE_COMPENSATION:
                    boolean isPaid = intent.getBooleanExtra(ParseQueryService.INTENT_COMPENSATION_PAID, false);
                    onCompensationsPinned(isPaid);
                    break;
                case ParseQueryService.DATA_TYPE_GROUP:
                    onGroupQueried();
                    break;
                case ParseQueryService.DATA_TYPE_TASK:
                    onTasksPinned();
                    break;
            }
        }
    };
    @NonNull
    private IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(@NonNull IabResult result, @NonNull Inventory inv) {
            if (result.isFailure()) {
                return;
            }

            Purchase premiumPurchase = inv.getPurchase(SKU_PREMIUM);
            mIsPremium = premiumPurchase != null && developerPayloadIsValid(premiumPurchase);
            mInTrialMode = !mIsPremium && freeAutoPurchasesAvailable();
            toggleGoPremiumVisibility();
        }
    };
    @NonNull
    private IabHelper.OnIabPurchaseFinishedListener mIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(@NonNull IabResult result, @NonNull Purchase info) {
            if (!result.isFailure() && developerPayloadIsValid(info) &&
                    info.getSku().equals(SKU_PREMIUM)) {
                mIsPremium = true;
                mInTrialMode = false;
            } else {
                mIsPremium = false;
                mInTrialMode = freeAutoPurchasesAvailable();
            }

            toggleGoPremiumVisibility();
        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    private boolean developerPayloadIsValid(@NonNull Purchase iabPurchase) {
        String payload = iabPurchase.getDeveloperPayload();
        return payload.equals(mCurrentUser.getObjectId());
    }

    private boolean freeAutoPurchasesAvailable() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        int freeAutoLimit = config.getInt(Config.FREE_PURCHASES_LIMIT);
        return mCurrentUser.getPremiumCount() < freeAutoLimit;
    }

    private void toggleGoPremiumVisibility() {
        MenuItem item = mNavigationViewMenu.findItem(R.id.nav_go_premium);
        if (mIsPremium) {
            item.setVisible(false);
        } else {
            item.setVisible(true);
        }
    }

    @CallSuper
    public void onPurchasesPinned() {
        // empty default implementation
    }

    @CallSuper
    public void onUsersPinned() {
        // empty default implementation
    }

    @CallSuper
    public void onCompensationsPinned(boolean isPaid) {
        // empty default implementation
    }

    @CallSuper
    public void onTasksPinned() {
        // empty default implementation
    }

    @CallSuper
    public void onGroupQueried() {
        updateGroupSpinnerList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserIsLoggedIn = checkUserLoggedIn();
    }

    private boolean checkUserLoggedIn() {
        if (ParseUser.getCurrentUser() == null) {
            startLoginActivity();
            return false;
        } else {
            return true;
        }
    }

    private void startLoginActivity() {
        Intent intentLogin = new Intent(this, LoginActivity.class);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            String invitedEmail = uri.getQueryParameter(URI_INVITED_EMAIL);
            intentLogin.putExtra(LoginActivity.INTENT_URI_EMAIL, invitedEmail);
        }
        if (intent.hasExtra(LoginActivity.INTENT_EXTRA_SIGN_UP)) {
            intentLogin.putExtra(LoginActivity.INTENT_EXTRA_SIGN_UP, true);
        }

//        Starting an activity with forResult and transitions during a lifecycle method results on
//        onActivityResult not being called
//        ActivityOptionsCompat activityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intentLogin, INTENT_REQUEST_LOGIN);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupNavDrawer();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navdrawer_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);

                mSelectedNavDrawerItem = menuItem.getItemId();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mNavigationViewMenu = navigationView.getMenu();

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

    final void fetchCurrentUserGroups() {
        User currentUser = (User) ParseUser.getCurrentUser();
        List<ParseObject> groups = null;
        if (currentUser != null) {
            groups = currentUser.getGroups();
        }
        if (groups == null || groups.isEmpty()) {
            onGroupsFetched();
            return;
        }

        mGroupsCount = groups.size();
        for (final ParseObject group : groups) {
            if (group.isDataAvailable()) {
                mFetchCounter++;
                checkFetchesFinished();
            } else {
                LocalQuery.fetchObjectData(group, this);
            }
        }
    }

    @Override
    public void onObjectFetched(@NonNull ParseObject object) {
        mFetchCounter++;
        checkFetchesFinished();
    }

    private void checkFetchesFinished() {
        if (mFetchCounter == mGroupsCount) {
            mFetchCounter = 0;
            onGroupsFetched();
        }
    }

    @CallSuper
    void onGroupsFetched() {
        setCurrentUserAndGroup();
        if (mCurrentUser != null) {
            setupNavDrawerHeader();
            if (!BuildConfig.DEBUG) {
                setupIab();
            }
        }
    }

    final void setCurrentUserAndGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null) {
            mCurrentUser = currentUser;
            mCurrentGroup = currentUser.getCurrentGroup();
        }
    }

    private void setupNavDrawerHeader() {
        mTextViewHeaderNickname = (TextView) findViewById(R.id.tv_drawer_nickname);
        mImageViewHeader = (ImageView) findViewById(R.id.iv_header_background);
        mImageViewHeaderAvatar = (ImageView) findViewById(R.id.iv_drawer_avatar);
        mImageViewHeaderAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileSettings();
            }
        });

        setAvatarAndNickname();
        setupNavDrawerHeaderGroupSpinner();
    }

    private void startProfileSettings() {
        Intent intent = new Intent(this, SettingsProfileActivity.class);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE,
                options.toBundle());
    }

    private void setAvatarAndNickname() {
        mTextViewHeaderNickname.setText(mCurrentUser.getNickname());

        byte[] avatarByteArray = mCurrentUser.getAvatar();
        final Context context = this;
        if (avatarByteArray != null) {
            Glide.with(this)
                    .load(avatarByteArray)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewHeaderAvatar) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setImageDrawable(Avatar.getRoundedDrawable(context, resource, true));
                        }
                    });

            Glide.with(this)
                    .load(avatarByteArray)
                    .bitmapTransform(new BlurTransformation(context))
                    .into(mImageViewHeader);
        } else {
            mImageViewHeaderAvatar.setImageDrawable(Avatar.getFallbackDrawable(context, true, true));
        }
    }

    private void setupNavDrawerHeaderGroupSpinner() {
        mSpinnerGroups = (Spinner) findViewById(R.id.sp_drawer_group);
        mSpinnerGroupsAdapter = new NavHeaderGroupsArrayAdapter(this, mGroups);
        mSpinnerGroups.setAdapter(mSpinnerGroupsAdapter);
        mSpinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                Group groupSelected = (Group) parent.getItemAtPosition(position);
                onGroupChanged(groupSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateGroupSpinner();
    }

    private void updateGroupSpinner() {
        updateGroupSpinnerList();
        updateGroupSpinnerPosition();
    }

    private void updateGroupSpinnerList() {
        mGroups.clear();

        User currentUser = (User) ParseUser.getCurrentUser();
        List<ParseObject> groups = currentUser.getGroups();
        if (!groups.isEmpty()) {
            mGroups.addAll(groups);
        }

        mSpinnerGroupsAdapter.notifyDataSetChanged();
    }

    final void updateGroupSpinnerPosition() {
        User currentUser = (User) ParseUser.getCurrentUser();
        int position = mSpinnerGroupsAdapter.getPosition(currentUser.getCurrentGroup());
        mSpinnerGroups.setSelection(position);
    }

    private void onGroupChanged(@NonNull ParseObject group) {
        Group oldGroup = mCurrentUser.getCurrentGroup();
        if (oldGroup.getObjectId().equals(group.getObjectId())) {
            return;
        }

        mCurrentGroup = (Group) group;
        mCurrentUser.setCurrentGroup(group);
        mCurrentUser.saveEventually();
        onNewGroupSet();
    }

    protected abstract void onNewGroupSet();

    private void setupIab() {
        String base64EncodedPublicKey = IabKey.getKey();

        mIabHelper = new IabHelper(this, base64EncodedPublicKey);
        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(@NonNull IabResult result) {
                if (result.isSuccess()) {
                    mIabHelper.queryInventoryAsync(true, mQueryInventoryFinishedListener);
                } else {
                    Log.e(LOG_TAG, "IabSetup failed with error " + result.getMessage());
                }
            }
        });
    }

    final void setStatusBarBackgroundColor(int color) {
        mDrawerLayout.setStatusBarBackgroundColor(color);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
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

    final void replaceDrawerIndicatorWithUp() {
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Un-check all enabled items in the NavDrawer.
     */
    final void uncheckNavDrawerItems() {
        mNavigationViewMenu.setGroupCheckable(R.id.nav_group_main, false, true);
    }

    /**
     * Check newly selected item in the NavDrawer.
     *
     * @param itemId the id of the newly selected item
     */
    final void checkNavDrawerItem(int itemId) {
        MenuItem item = mNavigationViewMenu.findItem(itemId);
        item.setChecked(true);
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
            case R.id.nav_go_premium:
                goPremium();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mIabHelper != null && mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    afterLoginSetup();
                } else {
                    finish();
                }
                break;
            case INTENT_REQUEST_SETTINGS:
                switch (resultCode) {
                    case SettingsFragment.RESULT_LOGOUT:
                        Intent intent = new Intent(this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case SettingsFragment.RESULT_GROUP_CHANGED:
                        updateGroupSpinner();
                        break;
                }
                break;
            case INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        MessageUtils.showBasicSnackbar(mSpinnerGroups,
                                getString(R.string.toast_changes_saved));
                        setAvatarAndNickname();
                        break;
                    case SettingsProfileActivity.RESULT_CHANGES_DISCARDED:
                        MessageUtils.showBasicSnackbar(mSpinnerGroups,
                                getString(R.string.toast_changes_discarded));
                        break;
                }
                break;
        }
    }

    @CallSuper
    void afterLoginSetup() {
        mUserIsLoggedIn = true;
        fetchCurrentUserGroups();

        // subclasses are free to add stuff here
    }

    final void goPremium() {
        if (mIabHelper == null || !mIabHelper.subscriptionsSupported()) {
            MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_not_supported));
            return;
        }

        String payload = mCurrentUser.getObjectId();
        mIabHelper.launchSubscriptionPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                mIabPurchaseFinishedListener, payload);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter(ParseQueryService.INTENT_FILTER_DATA_NEW));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIabHelper != null) {
            mIabHelper.dispose();
        }

        mIabHelper = null;
    }
}
