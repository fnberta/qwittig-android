/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.View;

import com.parse.ParseConfig;
import com.parse.ParseObject;
import com.parse.ParsePush;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.group.InvitedGroupHelper;
import ch.giantific.qwittig.data.helpers.query.MoreQueryHelper;
import ch.giantific.qwittig.data.helpers.query.PurchaseQueryHelper;
import ch.giantific.qwittig.domain.models.parse.Config;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.services.ParseQueryService;
import ch.giantific.qwittig.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GoPremiumDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupJoinDialogFragment;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.ViewUtils;

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
public class HomeActivity extends BaseNavDrawerActivity implements
        View.OnClickListener,
        HomeDraftsFragment.FragmentInteractionListener,
        GroupJoinDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        GoPremiumDialogFragment.DialogInteractionListener,
        PurchaseQueryHelper.HelperInteractionListener,
        InvitedGroupHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final String INVITED_GROUP_HELPER = "INVITED_GROUP_HELPER";
    private static final String URI_INVITED_GROUP_ID = "group";
    private static final String STATE_PURCHASE_FRAGMENT = "STATE_PURCHASE_FRAGMENT";
    private static final String STATE_DRAFTS_FRAGMENT = "STATE_DRAFTS_FRAGMENT";
    private static final String GROUP_JOIN_DIALOG = "GROUP_JOIN_DIALOG";
    private static final String CREATE_GROUP_DIALOG = "CREATE_GROUP_DIALOG";
    private static final String GO_PREMIUM_DIALOG = "GO_PREMIUM_DIALOG";
    private Group mInvitedGroup;
    private String mInviteInitiator;
    private String mInvitedGroupId;
    private ProgressDialog mProgressDialog;
    private HomePurchasesFragment mHomePurchasesFragment;
    private HomeDraftsFragment mHomeDraftsFragment;
    private FabMenu mFabMenu;
    private int mInvitationAction;

    @Override
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DATA_TYPE_PURCHASES_UPDATED) {
            onPurchasesUpdated();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_home);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        mFabMenu = (FabMenu) findViewById(R.id.fab_menu);
        mFabMenu.hideMenuButton(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFabMenu.showMenuButton(true);
            }
        }, ViewUtils.FAB_CIRCULAR_REVEAL_DELAY * 4);

        FloatingActionButton fabAuto = (FloatingActionButton) findViewById(R.id.fab_auto);
        fabAuto.setOnClickListener(this);
        FloatingActionButton fabManual = (FloatingActionButton) findViewById(R.id.fab_manual);
        fabManual.setOnClickListener(this);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
                checkForInvitations();
            } else {
                mHomePurchasesFragment = (HomePurchasesFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_PURCHASE_FRAGMENT);
                mHomeDraftsFragment = (HomeDraftsFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT);

                setupTabs();
            }
        }
    }

    private void addViewPagerFragments() {
        mHomePurchasesFragment = new HomePurchasesFragment();
        mHomeDraftsFragment = new HomeDraftsFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mHomePurchasesFragment, getString(R.string.tab_purchases));
        tabsAdapter.addFragment(mHomeDraftsFragment, getString(R.string.title_activity_purchase_drafts));
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If user is logged in, fragments will be added, hence save them
        if (mUserIsLoggedIn) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.putFragment(outState, STATE_PURCHASE_FRAGMENT, mHomePurchasesFragment);
            fragmentManager.putFragment(outState, STATE_DRAFTS_FRAGMENT, mHomeDraftsFragment);
        }
    }

    private void checkForInvitations() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String intentAction = intent.getAction();
        if (Intent.ACTION_VIEW.equals(intentAction)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String email = uri.getQueryParameter(URI_INVITED_EMAIL);
                mInvitedGroupId = uri.getQueryParameter(URI_INVITED_GROUP_ID);

                if (!email.equals(mCurrentUser.getUsername())) {
                    Snackbar.make(mFabMenu, R.string.toast_emails_no_match, Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
        } else if (intent.hasExtra(PushBroadcastReceiver.KEY_PUSH_DATA)) {
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                String notificationType = jsonExtras.optString(PushBroadcastReceiver.NOTIFICATION_TYPE);
                if (PushBroadcastReceiver.TYPE_USER_INVITED.equals(notificationType)) {
                    mInvitedGroupId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID);
                    mInviteInitiator = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_USER);
                    mInvitationAction = intent.getIntExtra(
                            PushBroadcastReceiver.INTENT_ACTION_INVITATION, 0);
                }
            } catch (JSONException e) {
                return;
            }
        } else {
            return;
        }

        if (!TextUtils.isEmpty(mInvitedGroupId)) {
            if (!Utils.isConnected(this)) {
                Snackbar.make(mFabMenu, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
                return;
            }

            // add currentUser to the ACL and Role of the group he is invited to, otherwise we
            // won't be able to query the group
            getInvitedGroupWithHelper();
        }
    }

    private void getInvitedGroupWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment invitedGroupHelper = HelperUtils.findHelper(fragmentManager, INVITED_GROUP_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (invitedGroupHelper == null) {
            invitedGroupHelper = InvitedGroupHelper.newInstance(mInvitedGroupId);

            fragmentManager.beginTransaction()
                    .add(invitedGroupHelper, INVITED_GROUP_HELPER)
                    .commit();
        }
    }

    @Override
    public void onInvitedGroupQueryFailed(int errorCode) {
        ParseErrorHandler.handleParseError(this, errorCode);
        Snackbar.make(mFabMenu, ParseErrorHandler.getErrorMessage(this, errorCode),
                Snackbar.LENGTH_LONG).show();
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);
    }

    @Override
    public void onEmailNotValid() {
        Snackbar.make(mFabMenu, getString(R.string.toast_group_invite_not_valid),
                Snackbar.LENGTH_LONG).show();
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);
    }

    @Override
    public void onInvitedGroupQueried(@NonNull ParseObject parseObject) {
        mInvitedGroup = (Group) parseObject;

        switch (mInvitationAction) {
            case PushBroadcastReceiver.ACTION_INVITATION_ACCEPTED:
                onJoinInvitedGroupSelected();
                break;
            case PushBroadcastReceiver.ACTION_INVITATION_DISCARDED:
                onDiscardInvitationSelected();
                break;
            default:
                String groupName = mInvitedGroup.getName();
                showGroupJoinDialog(groupName);
        }
    }

    private void showGroupJoinDialog(String groupName) {
        GroupJoinDialogFragment groupJoinDialogFragment =
                GroupJoinDialogFragment.newInstance(groupName, mInviteInitiator);
        groupJoinDialogFragment.show(getFragmentManager(), GROUP_JOIN_DIALOG);
    }

    @Override
    public void onJoinInvitedGroupSelected() {
        showProgressDialog(getString(R.string.progress_switch_groups));

        InvitedGroupHelper helper = (InvitedGroupHelper)
                HelperUtils.findHelper(getFragmentManager(), INVITED_GROUP_HELPER);
        helper.joinInvitedGroup(mInvitedGroup);
    }

    private void showProgressDialog(String message) {
        mProgressDialog = ProgressDialog.show(this, null, message);
    }

    @Override
    public void onUserJoinedGroup() {
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);
        dismissProgressDialog();
        Snackbar.make(mFabMenu, getString(R.string.toast_group_added, mInvitedGroup.getName()),
                Snackbar.LENGTH_LONG).show();

        // register for notifications for the new group
        ParsePush.subscribeInBackground(mInvitedGroup.getObjectId());

        // remove user from invited list
        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        mCurrentGroup = mCurrentUser.getCurrentGroup();
        updateGroupSpinner();
        queryAll();
    }

    private void queryAll() {
        if (userIsInGroup()) {
            mHomePurchasesFragment.setOnlineQueryInProgress(true);
            ParseQueryService.startQueryAll(this);
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onUserJoinGroupFailed(int errorCode) {
        ParseErrorHandler.handleParseError(this, errorCode);
        Snackbar.make(mFabMenu, ParseErrorHandler.getErrorMessage(this, errorCode), Snackbar.LENGTH_LONG).show();
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);

        dismissProgressDialog();
    }

    @Override
    public void onDiscardInvitationSelected() {
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);

        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        Snackbar.make(mFabMenu, R.string.toast_invitation_discarded, Snackbar.LENGTH_LONG).show();
    }

    @Override
    void afterLoginSetup() {
        super.afterLoginSetup();

        addViewPagerFragments();
        queryAll();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab_auto: {
                if (userIsInGroup()) {
                    if (mIsPremium || mInTrialMode || BuildConfig.DEBUG) {
                        Intent intent = new Intent(this, PurchaseAddActivity.class);
                        intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, true);
                        intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, mInTrialMode);
                        startActivityForResult(intent,
                                HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY);
                    } else {
                        showGoPremiumDialog();
                    }
                } else {
                    showCreateGroupDialog();
                }
                break;
            }
            case R.id.fab_manual: {
                if (userIsInGroup()) {
                    Intent intent = new Intent(this, PurchaseAddActivity.class);
                    intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, false);
                    ActivityOptionsCompat activityOptionsCompat =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this);
                    startActivityForResult(intent,
                            HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                            activityOptionsCompat.toBundle());
                } else {
                    showCreateGroupDialog();
                }
                break;
            }
        }

        mFabMenu.close();
    }

    private void showCreateGroupDialog() {
        GroupCreateDialogFragment groupCreateDialogFragment =
                GroupCreateDialogFragment.newInstance(R.string.dialog_group_create_purchases);
        groupCreateDialogFragment.show(getFragmentManager(), CREATE_GROUP_DIALOG);
    }

    @Override
    public void onCreateGroupSelected() {
        Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    private void showGoPremiumDialog() {
        GoPremiumDialogFragment goPremiumDialogFragment = new GoPremiumDialogFragment();
        goPremiumDialogFragment.show(getFragmentManager(), GO_PREMIUM_DIALOG);
    }

    @Override
    public void onGoPremiumSelected() {
        goPremium();
    }

    @Override
    public void onPurchaseUpdateFailed(int errorCode) {
        mHomePurchasesFragment.onPurchaseUpdateFailed(errorCode);
    }

    @Override
    public void onPurchasesUpdated() {
        // after login or user joined a new group, this will be set to true, hence set false
        mHomePurchasesFragment.setOnlineQueryInProgress(false);
        mHomePurchasesFragment.onPurchasesUpdated();
    }

    @Override
    public void onAllPurchasesUpdated() {
        mHomePurchasesFragment.onAllPurchasesUpdated();
    }

    @Override
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> objects) {
        mHomePurchasesFragment.onMoreObjectsLoaded(objects);
    }

    @Override
    public void onMoreObjectsLoadFailed(int errorCode) {
        mHomePurchasesFragment.onMoreObjectsLoadFailed(errorCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseAddFragment.RESULT_PURCHASE_SAVED:
                        Snackbar.make(mFabMenu, R.string.toast_purchase_added, Snackbar.LENGTH_LONG).show();
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_SAVED_AUTO:
                        String purchaseAdded = getString(R.string.toast_purchase_added);
                        if (mInTrialMode) {
                            ParseConfig config = ParseConfig.getCurrentConfig();
                            int freeAutoLimit = config.getInt(Config.FREE_PURCHASES_LIMIT);
                            int freePurchasesLeft = freeAutoLimit - mCurrentUser.getPremiumCount();
                            purchaseAdded += ". " + getString(R.string.toast_free_purchases_left, freePurchasesLeft);
                        }
                        Snackbar.make(mFabMenu, purchaseAdded, Snackbar.LENGTH_LONG).show();
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_DRAFT:
                        Snackbar.make(mFabMenu, R.string.toast_purchase_added_draft,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_DISCARDED:
                        Snackbar.make(mFabMenu, R.string.toast_purchase_discarded,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_ERROR:
                        Snackbar.make(mFabMenu, R.string.toast_create_image_file_failed,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
            case INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsActivity.RESULT_PURCHASE_DELETED:
                        Snackbar.make(mFabMenu, R.string.toast_purchase_deleted,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case PurchaseDetailsActivity.RESULT_GROUP_CHANGED:
                        updateGroupSpinnerPosition();
                        break;
                }
                break;
        }
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mHomeDraftsFragment);
    }

    @Override
    protected void onNewGroupSet() {
        mHomePurchasesFragment.updateFragment();
        mHomeDraftsFragment.updateFragment();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}

