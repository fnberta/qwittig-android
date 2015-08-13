package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;

import org.apache.commons.math3.fraction.BigFraction;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.helper.FullQueryHelper;
import ch.giantific.qwittig.helper.InvitedGroupHelper;
import ch.giantific.qwittig.helper.MoreQueryHelper;
import ch.giantific.qwittig.ui.adapter.TabsAdapter;
import ch.giantific.qwittig.ui.dialogs.GoPremiumDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupJoinDialogFragment;
import ch.giantific.qwittig.ui.widgets.FabSpeedDial;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;


public class HomeActivity extends BaseNavDrawerActivity implements
        View.OnClickListener,
        HomeBaseFragment.FragmentInteractionListener,
        GroupJoinDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        GoPremiumDialogFragment.DialogInteractionListener,
        FullQueryHelper.HelperInteractionListener,
        InvitedGroupHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener {

    private static final String INVITED_GROUP_HELPER = "invited_group_helper";
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    @IntDef({ADAPTER_ALL, ADAPTER_PURCHASES, ADAPTER_USER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentAdapter {}
    public static final int ADAPTER_ALL = 0;
    public static final int ADAPTER_PURCHASES = 1;
    public static final int ADAPTER_USER = 2;

    private static final String URI_INVITED_GROUP_ID = "group";
    private static final String PURCHASE_FRAGMENT = "purchase_fragment";
    private static final String USER_FRAGMENT = "user_fragment";
    private TextView mBalance;
    private Group mInvitedGroup;
    private String mInviteInitiator;
    private String mInvitedGroupId;
    private ProgressDialog mProgressDialog;
    private HomePurchasesFragment mHomePurchasesFragment;
    private HomeUsersFragment mHomeUsersFragment;
    private TabLayout mTabLayout;
    private FloatingActionButton mFabAddPurchase;
    private FabSpeedDial mFabAddPurchaseAuto;
    private FabSpeedDial mFabAddPurchaseManual;
    private boolean mNewQueryNeeded;
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

        getSupportActionBar().setTitle(null);

        mBalance = (TextView) findViewById(R.id.tv_balance);
        mFabAddPurchase = (FloatingActionButton) findViewById(R.id.fab_group_new);
        mFabAddPurchase.setOnClickListener(this);
        if (Utils.isRunningLollipopAndHigher()) {
            mFabAddPurchase.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    circularRevealFab();
                }
            });
        } else {
            mFabAddPurchase.setVisibility(View.VISIBLE);
        }
        mFabAddPurchaseAuto = (FabSpeedDial) findViewById(R.id.fab_auto);
        mFabAddPurchaseAuto.setOnClickListener(this);
        mFabAddPurchaseManual = (FabSpeedDial) findViewById(R.id.fab_manual);
        mFabAddPurchaseManual.setOnClickListener(this);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
            } else {
                mHomePurchasesFragment = (HomePurchasesFragment) getFragmentManager()
                        .getFragment(savedInstanceState, PURCHASE_FRAGMENT);
                mHomeUsersFragment = (HomeUsersFragment) getFragmentManager()
                        .getFragment(savedInstanceState, USER_FRAGMENT);

                setupTabs();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealFab() {
        Animator reveal = Utils.getCircularRevealAnimator(mFabAddPurchase);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFabAddPurchase.setVisibility(View.VISIBLE);
            }
        });
        reveal.start();
    }

    private void addViewPagerFragments() {
        mHomePurchasesFragment = new HomePurchasesFragment();
        mHomeUsersFragment = new HomeUsersFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mHomePurchasesFragment, getString(R.string.tab_purchases));
        tabsAdapter.addFragment(mHomeUsersFragment, getString(R.string.tab_users));
        viewPager.setAdapter(tabsAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Check if fragments are added because they might not be when LoginActivity is started in
        // HomeActivity.
        if (mHomePurchasesFragment != null && mHomePurchasesFragment.isAdded() &&
                mHomeUsersFragment != null && mHomeUsersFragment.isAdded()) {
            getFragmentManager().putFragment(outState, PURCHASE_FRAGMENT, mHomePurchasesFragment);
            getFragmentManager().putFragment(outState, USER_FRAGMENT, mHomeUsersFragment);
        }
    }

    @Override
    public void onGroupsFetched() {
        super.onGroupsFetched();

        setToolbarHeader();

        if (mCheckForInvitations) {
            checkForInvitations();
        }

        if (mNewQueryNeeded) {
            onlineQuery();
            mNewQueryNeeded = false;
        }
    }

    private void setToolbarHeader() {
        BigFraction balance = BigFraction.ZERO;
        if (mCurrentUser != null) {
            balance = mCurrentUser.getBalance(mCurrentGroup);
        }
        mBalance.setText(MoneyUtils.formatMoney(balance, ParseUtils.getGroupCurrency()));
        setColorTheme(balance);
    }

    private void setColorTheme(BigFraction balance) {
        int color;
        int colorDark;
        int style;
        if (Utils.isPositive(balance)) {
            color = getResources().getColor(R.color.green);
            colorDark = getResources().getColor(R.color.green_dark);
            style = R.style.AppTheme_WithNavDrawer_Green;
        } else {
            color = getResources().getColor(R.color.red);
            colorDark = getResources().getColor(R.color.red_dark);
            style = R.style.AppTheme_WithNavDrawer_Red;
        }
        setTheme(style);
        mToolbar.setBackgroundColor(color);
        mTabLayout.setBackgroundColor(color);
        setStatusBarBackgroundColor(colorDark);
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
                    MessageUtils.showBasicSnackbar(mFabAddPurchase, getString(R.string.toast_emails_no_match));
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
                e.printStackTrace();
            }
        } else {
            return;
        }

        if (!TextUtils.isEmpty(mInvitedGroupId)) {
            if (!Utils.isConnected(this)) {
                MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_no_connection));
                return;
            }

            // add currentUser to the ACL and Role of the group he is invited to, otherwise we
            // won't be able to query the group
            getInvitedGroupWithHelper();
        }
    }

    private void getInvitedGroupWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        InvitedGroupHelper invitedGroupHelper = findInvitedGroupHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (invitedGroupHelper == null) {
            invitedGroupHelper = InvitedGroupHelper.newInstance(mInvitedGroupId);

            fragmentManager.beginTransaction()
                    .add(invitedGroupHelper, INVITED_GROUP_HELPER)
                    .commit();
        }
    }

    private InvitedGroupHelper findInvitedGroupHelper(FragmentManager fragmentManager) {
        return (InvitedGroupHelper) fragmentManager.findFragmentByTag(INVITED_GROUP_HELPER);
    }

    @Override
    public void onInvitedGroupQueryFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        MessageUtils.showBasicSnackbar(mFabAddPurchase, ParseErrorHandler.getErrorMessage(this, e));
        removeQueryHelper();
    }

    @Override
    public void onEmailNotValid() {
        MessageUtils.showBasicSnackbar(mFabAddPurchase, getString(R.string.toast_group_invite_not_valid));
        removeInvitedGroupHelper();
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

        findInvitedGroupHelper(getFragmentManager()).joinInvitedGroup(mInvitedGroup);
    }

    private void showProgressDialog(String message) {
        mProgressDialog = MessageUtils.getProgressDialog(this, message);
        mProgressDialog.show();
    }

    @Override
    public void onUserJoinedGroup() {
        removeInvitedGroupHelper();

        // register for notifications for the new group
        ParsePush.subscribeInBackground(mInvitedGroup.getObjectId());

        // remove user from invited list
        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        mNewQueryNeeded = true;
        mCheckForInvitations = false;
        updateCurrentUserGroups();
    }

    @Override
    public void onUserJoinGroupFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        MessageUtils.getBasicSnackbar(mToolbar, ParseErrorHandler.getErrorMessage(this, e));
        removeInvitedGroupHelper();

        setLoading(false);
    }

    private void removeInvitedGroupHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        InvitedGroupHelper invitedGroupHelper = findInvitedGroupHelper(fragmentManager);

        if (invitedGroupHelper != null) {
            fragmentManager.beginTransaction().remove(invitedGroupHelper).commitAllowingStateLoss();
        }
    }

    @Override
    public void discardInvitation() {
        removeInvitedGroupHelper();

        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
        mInvitedGroup.saveEventually();

        MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_invitation_discarded));
    }

    @Override
    void afterLoginSetup() {
        mNewQueryNeeded = true;
        addViewPagerFragments();

        super.afterLoginSetup();
    }

    /**
     * Calls CloudFunction to calculate balances for users in currentGroup
     */
    @Override
    public void onlineQuery() {
        if (!Utils.isConnected(this)) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        FullQueryHelper fullQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fullQueryHelper == null) {
            fullQueryHelper = new FullQueryHelper();

            fragmentManager.beginTransaction()
                    .add(fullQueryHelper, FullQueryHelper.FULL_QUERY_HELPER)
                    .commit();
        }
    }

    private FullQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (FullQueryHelper) fragmentManager.findFragmentByTag(FullQueryHelper.FULL_QUERY_HELPER);
    }

    @Override
    public void onPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(this, e));
        removeQueryHelper();

        setLoading(false);
    }

    @Override
    public void onAllQueriesFinished() {
        removeQueryHelper();
        setLoading(false);
    }

    private void setLoading(boolean isLoading) {
        mHomeUsersFragment.setLoading(isLoading);
        mHomePurchasesFragment.setLoading(isLoading);

        if (!isLoading && mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        FullQueryHelper fullQueryHelper = findQueryHelper(fragmentManager);

        if (fullQueryHelper != null) {
            fragmentManager.beginTransaction().remove(fullQueryHelper).commitAllowingStateLoss();
        }
    }

    private void showOnlineQueryErrorSnackbar(String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mToolbar, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        });
        snackbar.show();
    }

    @Override
    public void onUsersPinned() {
        super.onUsersPinned();

        updateFragmentAdapters(ADAPTER_USER);
        setToolbarHeader();
    }

    @Override
    public void onPurchasesPinned() {
        super.onPurchasesPinned();

        updateFragmentAdapters(ADAPTER_PURCHASES);
    }

    private void updateFragmentAdapters(@FragmentAdapter int adapter) {
        switch (adapter) {
            case ADAPTER_ALL:
                mHomePurchasesFragment.updateAdapter();
                mHomeUsersFragment.updateAdapter();
                break;
            case ADAPTER_PURCHASES:
                mHomePurchasesFragment.updateAdapter();
                break;
            case ADAPTER_USER:
                mHomeUsersFragment.updateAdapter();
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_group_new: {
                toggleFabVisibility();
                break;
            }
            case R.id.fab_auto: {
                if (userIsInGroup()) {
                    if (mIsPremium || mInTrialMode) {
                        Intent intent = new Intent(this, PurchaseAddActivity.class);
                        intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, true);
                        intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, mInTrialMode);
                        startActivityForResult(intent,
                                HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY);
                    } else {
                        showGoPremiumDialog();
                    }
                }
                toggleFabVisibility();
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
                toggleFabVisibility();
                break;
            }
        }
    }

    private void toggleFabVisibility() {
        if (mFabAddPurchaseAuto.getVisibility() == View.VISIBLE &&
                mFabAddPurchaseManual.getVisibility() == View.VISIBLE) {
            mFabAddPurchaseAuto.hideSpeedDial();
            mFabAddPurchaseManual.hideSpeedDial();
            mFabAddPurchase.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_shopping_cart_black_24dp));
        } else {
            mFabAddPurchaseAuto.showSpeedDial();
            mFabAddPurchaseManual.showSpeedDial();
            mFabAddPurchase.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_clear_black_24dp));
        }
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
        GroupCreateDialogFragment groupCreateDialogFragment = new GroupCreateDialogFragment();
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
            case HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseAddActivity.RESULT_PURCHASE_SAVED:
                        MessageUtils.showBasicSnackbar(mFabAddPurchase, getString(R.string.toast_purchase_added));
                        break;
                    case PurchaseAddActivity.RESULT_PURCHASE_SAVED_AUTO:
                        String purchaseAdded = getString(R.string.toast_purchase_added);
                        if (mInTrialMode) {
                            ParseConfig config = ParseConfig.getCurrentConfig();
                            int freeAutoLimit = config.getInt(Config.FREE_PURCHASES_LIMIT);
                            int freePurchasesLeft = freeAutoLimit - mCurrentUser.getPremiumCount();
                            purchaseAdded += ". " + getString(R.string.toast_free_purchases_left, freePurchasesLeft);
                        }
                        MessageUtils.showBasicSnackbar(mFabAddPurchase, purchaseAdded);
                        break;
                    case PurchaseAddActivity.RESULT_PURCHASE_DRAFT:
                        MessageUtils.showBasicSnackbar(mFabAddPurchase,
                                getString(R.string.toast_purchase_added_draft));
                        break;
                    case PurchaseAddActivity.RESULT_PURCHASE_DISCARDED:
                        MessageUtils.showBasicSnackbar(mFabAddPurchase,
                                getString(R.string.toast_purchase_discarded));
                        break;
                    case PurchaseAddActivity.RESULT_PURCHASE_ERROR:
                        MessageUtils.showBasicSnackbar(mFabAddPurchase,
                                getString(R.string.toast_create_image_file_failed));
                        break;
                }
                break;
            case HomeActivity.INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsActivity.RESULT_PURCHASE_DELETED:
                        MessageUtils.showBasicSnackbar(mFabAddPurchase,
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
        updateCurrentUserGroups();
        updateFragmentAdapters(ADAPTER_ALL);
        setToolbarHeader();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}

