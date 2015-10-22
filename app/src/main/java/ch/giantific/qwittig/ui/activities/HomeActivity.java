package ch.giantific.qwittig.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;

import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.helpers.FullQueryHelper;
import ch.giantific.qwittig.helpers.InvitedGroupHelper;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.helpers.PurchaseQueryHelper;
import ch.giantific.qwittig.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.GoPremiumDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupJoinDialogFragment;
import ch.giantific.qwittig.utils.AnimUtils;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;


public class HomeActivity extends BaseNavDrawerActivity implements
        View.OnClickListener,
        HomePurchasesFragment.FragmentInteractionListener,
        GroupJoinDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        GoPremiumDialogFragment.DialogInteractionListener,
        PurchaseQueryHelper.HelperInteractionListener,
        InvitedGroupHelper.HelperInteractionListener,
        FullQueryHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final String INVITED_GROUP_HELPER = "invited_group_helper";
    private static final String URI_INVITED_GROUP_ID = "group";
    private static final String STATE_PURCHASE_FRAGMENT = "purchase_fragment";
    private static final String STATE_DRAFTS_FRAGMENT = "drafts_fragment";
    private static final String FULL_QUERY_HELPER = "full_query_helper";
    private Group mInvitedGroup;
    private String mInviteInitiator;
    private String mInvitedGroupId;
    private ProgressDialog mProgressDialog;
    private HomePurchasesFragment mHomePurchasesFragment;
    private HomeDraftsFragment mHomeDraftsFragment;
    private FabMenu mFabMenu;
    private boolean mNewQueryNeeded = false;
    private boolean mCheckForInvitations = true;
    private int mInvitationAction;

    @Override
    public boolean isNewQueryNeeded() {
        return mNewQueryNeeded;
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
        }, AnimUtils.FAB_CIRCULAR_REVEAL_DELAY * 4);

        FloatingActionButton fabAuto = (FloatingActionButton) findViewById(R.id.fab_auto);
        fabAuto.setOnClickListener(this);
        FloatingActionButton fabManual = (FloatingActionButton) findViewById(R.id.fab_manual);
        fabManual.setOnClickListener(this);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
            } else {
                mHomePurchasesFragment = (HomePurchasesFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_PURCHASE_FRAGMENT);
                mHomeDraftsFragment = (HomeDraftsFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_DRAFTS_FRAGMENT);

                setupTabs();
            }

            fetchCurrentUserGroups();
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

    @Override
    public void onGroupsFetched() {
        super.onGroupsFetched();

        if (mCheckForInvitations) {
            checkForInvitations();
        }

        if (mNewQueryNeeded) {
            fullOnlineQueryWithHelper();
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
                    MessageUtils.showBasicSnackbar(mFabMenu, getString(R.string.toast_emails_no_match));
                    return;
                }
            }
        } else if (intent.hasExtra(PushBroadcastReceiver.KEY_PUSH_DATA)) {
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                String notificationType = jsonExtras.optString(PushBroadcastReceiver.NOTIFICATION_TYPE);
                if (PushBroadcastReceiver.TYPE_USER_INVITED.equals(notificationType)) {
                    mInvitedGroupId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_GROUP);
                    mInviteInitiator = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_USER);
                    mInvitationAction = intent.getIntExtra(
                            PushBroadcastReceiver.INTENT_ACTION_INVITATION, 0);
                }
            } catch (JSONException e) {
                // TODO: do we need to handle this?
            }
        } else {
            return;
        }

        if (!TextUtils.isEmpty(mInvitedGroupId)) {
            if (!Utils.isConnected(this)) {
                MessageUtils.showBasicSnackbar(mFabMenu, getString(R.string.toast_no_connection));
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
    public void onInvitedGroupQueryFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        MessageUtils.showBasicSnackbar(mFabMenu, ParseErrorHandler.getErrorMessage(this, e));
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);
    }

    @Override
    public void onEmailNotValid() {
        MessageUtils.showBasicSnackbar(mFabMenu, getString(R.string.toast_group_invite_not_valid));
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);
    }

    @Override
    public void onInvitedGroupQueried(ParseObject parseObject) {
        mInvitedGroup = (Group) parseObject;

        switch (mInvitationAction) {
            case PushBroadcastReceiver.ACTION_INVITATION_ACCEPTED:
                joinInvitedGroup();
                break;
            case PushBroadcastReceiver.ACTION_INVITATION_DISCARDED:
                discardInvitation();
                break;
            default:
                String groupName = mInvitedGroup.getName();
                showGroupJoinDialog(groupName);
        }
    }

    private void showGroupJoinDialog(String groupName) {
        GroupJoinDialogFragment groupJoinDialogFragment =
                GroupJoinDialogFragment.newInstance(groupName, mInviteInitiator);
        groupJoinDialogFragment.show(getFragmentManager(), "group_join");
    }

    @Override
    public void joinInvitedGroup() {
        showProgressDialog(getString(R.string.progress_switch_groups));

        InvitedGroupHelper helper = (InvitedGroupHelper)
                HelperUtils.findHelper(getFragmentManager(), INVITED_GROUP_HELPER);
        helper.joinInvitedGroup(mInvitedGroup);
    }

    private void showProgressDialog(String message) {
        mProgressDialog = MessageUtils.getProgressDialog(this, message);
        mProgressDialog.show();
    }

    @Override
    public void onUserJoinedGroup() {
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);

        // register for notifications for the new group
        ParsePush.subscribeInBackground(mInvitedGroup.getObjectId());

        // remove user from invited list
        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        mNewQueryNeeded = true;
        mCheckForInvitations = false;
        fetchCurrentUserGroups();
    }

    @Override
    public void onUserJoinGroupFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        MessageUtils.getBasicSnackbar(mFabMenu, ParseErrorHandler.getErrorMessage(this, e));
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);

        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void discardInvitation() {
        HelperUtils.removeHelper(getFragmentManager(), INVITED_GROUP_HELPER);

        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        MessageUtils.showBasicSnackbar(mFabMenu, getString(R.string.toast_invitation_discarded));
    }

    private void fullOnlineQueryWithHelper() {
        if (!Utils.isConnected(this)) {
            dismissProgressDialog();
            showFullOnlineQueryErrorSnackbar();
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fullQueryHelper = HelperUtils.findHelper(fragmentManager, FULL_QUERY_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fullQueryHelper == null) {
            fullQueryHelper = new FullQueryHelper();

            fragmentManager.beginTransaction()
                    .add(fullQueryHelper, FULL_QUERY_HELPER)
                    .commit();
        }
    }

    private void showFullOnlineQueryErrorSnackbar() {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mFabMenu, getString(R.string.toast_failed_load_new_group_data));
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog(getString(R.string.progress_new_group_data));
                fullOnlineQueryWithHelper();
            }
        });
        snackbar.show();

    }

    @Override
    public void onPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        showFullOnlineQueryErrorSnackbar();
        HelperUtils.removeHelper(getFragmentManager(), FULL_QUERY_HELPER);

        dismissProgressDialog();
    }

    @Override
    public void onFullQueryFinished(boolean failedEarly) {
        HelperUtils.removeHelper(getFragmentManager(), FULL_QUERY_HELPER);

        dismissProgressDialog();

        if (failedEarly) {
            mNewQueryNeeded = false;
            updateFragmentAdapters();
        }
    }

    private void updateFragmentAdapters() {
        mHomePurchasesFragment.updateAdapter();
        mHomeDraftsFragment.updateAdapter();
    }

    @Override
    void afterLoginSetup() {
        mNewQueryNeeded = true;
        addViewPagerFragments();

        super.afterLoginSetup();
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
                }
                break;
            }
        }

        mFabMenu.close();
    }

    private boolean userIsInGroup() {
        if (mCurrentUser == null) {
            return false;
        }

        if (mCurrentGroup == null) {
            showCreateGroupDialog();
            return false;
        }

        return true;
    }

    private void showCreateGroupDialog() {
        GroupCreateDialogFragment groupCreateDialogFragment = GroupCreateDialogFragment.newInstance(R.string.dialog_group_create_purchases);
        groupCreateDialogFragment.show(getFragmentManager(), "create_group");
    }

    /**
     * Called from dialog that is shown when user tries to add new purchase and is not yet part of
     * any group.
     */
    @Override
    public void createNewGroup() {
        Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    private void showGoPremiumDialog() {
        GoPremiumDialogFragment goPremiumDialogFragment = new GoPremiumDialogFragment();
        goPremiumDialogFragment.show(getFragmentManager(), "go_premium");

        // goPremium() is handled in BaseNavDrawerActivity
    }

    @Override
    public void onPurchasesPinFailed(ParseException e) {
        mHomePurchasesFragment.onPurchasesPinFailed(e);
    }

    @Override
    public void onPurchasesPinned() {
        super.onPurchasesPinned();

        // will be set to true after login and group change
        mNewQueryNeeded = false;

        mHomePurchasesFragment.onPurchasesPinned();
    }

    @Override
    public void onAllPurchasesQueriesFinished() {
        mHomePurchasesFragment.onAllPurchasesQueriesFinished();
    }

    @Override
    public void onMoreObjectsPinned(List<ParseObject> objects) {
        mHomePurchasesFragment.onMoreObjectsPinned(objects);
    }

    @Override
    public void onMoreObjectsPinFailed(ParseException e) {
        mHomePurchasesFragment.onMoreObjectsPinFailed(e);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseAddFragment.RESULT_PURCHASE_SAVED:
                        MessageUtils.showBasicSnackbar(mFabMenu, getString(R.string.toast_purchase_added));
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_SAVED_AUTO:
                        String purchaseAdded = getString(R.string.toast_purchase_added);
                        if (mInTrialMode) {
                            ParseConfig config = ParseConfig.getCurrentConfig();
                            int freeAutoLimit = config.getInt(Config.FREE_PURCHASES_LIMIT);
                            int freePurchasesLeft = freeAutoLimit - mCurrentUser.getPremiumCount();
                            purchaseAdded += ". " + getString(R.string.toast_free_purchases_left, freePurchasesLeft);
                        }
                        MessageUtils.showBasicSnackbar(mFabMenu, purchaseAdded);
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_DRAFT:
                        MessageUtils.showBasicSnackbar(mFabMenu,
                                getString(R.string.toast_purchase_added_draft));
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_DISCARDED:
                        MessageUtils.showBasicSnackbar(mFabMenu,
                                getString(R.string.toast_purchase_discarded));
                        break;
                    case PurchaseAddFragment.RESULT_PURCHASE_ERROR:
                        MessageUtils.showBasicSnackbar(mFabMenu,
                                getString(R.string.toast_create_image_file_failed));
                        break;
                }
                break;
            case INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsActivity.RESULT_PURCHASE_DELETED:
                        MessageUtils.showBasicSnackbar(mFabMenu,
                                getString(R.string.toast_purchase_deleted));
                        break;
                    case PurchaseDetailsActivity.RESULT_GROUP_CHANGED:
                        updateGroupSpinnerPosition();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onNewGroupSet() {
        updateFragmentAdapters();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}

